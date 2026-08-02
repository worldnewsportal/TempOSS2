package com.yourname.tempmail.data.repo

import com.yourname.tempmail.data.db.MessageEntity
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.data.daos.MessageDao
import com.yourname.tempmail.domain.EmailMessage
import com.yourname.tempmail.domain.Lifetime
import com.yourname.tempmail.domain.Mailbox
import com.yourname.tempmail.domain.ProviderResult
import com.yourname.tempmail.providers.ProviderManager
import com.yourname.tempmail.util.RateLimiter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Message + attachment caching. Sync pulls provider summaries, upserts into Room,
 * and can hydrate full bodies on demand. Offline cache is always available; we
 * never pretend cached data was freshly synchronized (rule #22).
 */
class MessageRepository(
    private val db: TempmailDatabase,
    private val providers: ProviderManager,
    private val rateLimiter: RateLimiter = RateLimiter(),
) {
    private val messageDao: MessageDao = db.messageDao()

    fun observeInbox(mailboxId: Long): Flow<List<MessageEntity>> = messageDao.observeInbox(mailboxId)
    fun observeOne(messageId: Long): Flow<MessageEntity?> = db.messageDao().observeById(messageId)
    fun observeUnread(mailboxId: Long): Flow<List<MessageEntity>> = messageDao.observeUnread(mailboxId)
    fun observeStarred(mailboxId: Long): Flow<List<MessageEntity>> = messageDao.observeStarred(mailboxId)
    fun observeByLabel(mailboxId: Long, label: String): Flow<List<MessageEntity>> = messageDao.observeByLabel(mailboxId, label)
    fun search(mailboxId: Long, q: String): Flow<List<MessageEntity>> = messageDao.search(mailboxId, q)

    suspend fun message(id: Long): MessageEntity? = messageDao.byId(id)

    /** Pull newest summaries for a single mailbox. Returns count that changed. */
    suspend fun syncMailbox(mailbox: Mailbox): Int {
        if (Lifetime.isExpired(mailbox)) return 0
        val provider = providers.providerFor(mailbox.providerId) ?: return 0
        if (!rateLimiter.allow(mailbox.providerId)) return 0

        val result = provider.listMessages(mailbox)
        if (result !is ProviderResult.Success) return 0
        val thumbs = result.data
        val existingSeen = messageDao.allForMailbox(mailbox.id)
            .map { it.providerRawId }.toSet()

        var changed = 0
        thumbs.forEach { t ->
            if (t.providerRawId !in existingSeen) {
                messageDao.insert(
                    MessageEntity(
                        mailboxId = mailbox.id,
                        providerRawId = t.providerRawId,
                        fromName = t.from.name,
                        fromAddress = t.from.address,
                        subject = t.subject,
                        preview = t.preview,
                        date = t.date,
                        label = "INBOX",
                    )
                )
                changed++
            }
        }
        // Refresh unread count + lastSyncedAt on the mailbox so the UI shows fresh state.
        val unread = messageDao.unreadCount(mailbox.id)
        db.mailboxDao().byId(mailbox.id)?.let { e ->
            db.mailboxDao().update(e.copy(unreadCount = unread, lastSyncedAt = System.currentTimeMillis()))
        }
        return changed
    }

    suspend fun refreshAll(mailboxes: List<Mailbox>): Int {
        var total = 0
        mailboxes.forEach { total += refreshMailbox(it) }
        return total
    }

    suspend fun refreshMailbox(mailbox: Mailbox): Int = syncMailbox(mailbox)

    /** Fetch and cache the complete message including attachments. */
    suspend fun fetchFull(mailbox: Mailbox, messageId: Long): EmailMessage? {
        val local = messageDao.byId(messageId) ?: return null
        val provider = providers.providerFor(mailbox.providerId) ?: return null
        val full = when (val r = provider.readMessage(mailbox, local.providerRawId)) {
            is ProviderResult.Success -> r.data
            is ProviderResult.Failure -> return null
        }
        messageDao.update(
            MessageEntity(
                id = local.id,
                mailboxId = local.mailboxId,
                providerRawId = local.providerRawId,
                fromName = full.from.name,
                fromAddress = full.from.address,
                to = full.to.joinToString(";") { it.display },
                cc = full.cc.joinToString(";") { it.display },
                bcc = full.bcc.joinToString(";") { it.display },
                subject = full.subject,
                preview = full.preview,
                bodyText = full.bodyText,
                bodyHtml = full.bodyHtml,
                date = full.date,
                seen = local.seen,
                starred = local.starred,
                label = local.label,
                messageIdHeader = full.messageIdHeader,
                inReplyTo = full.inReplyTo,
            )
        )
        return full
    }

    suspend fun markSeen(mailbox: Mailbox, messageId: Long, seen: Boolean) {
        messageDao.setSeen(messageId, seen)
        providers.providerFor(mailbox.providerId)?.run {
            val e = messageDao.byId(messageId) ?: return@run
            markSeen(mailbox, e.providerRawId, seen)
        }
    }

    suspend fun markStarred(messageId: Long, starred: Boolean) = messageDao.setStarred(messageId, starred)

    suspend fun setLabel(messageId: Long, label: String?) = messageDao.setLabel(messageId, label)

    suspend fun delete(messageId: Long, mailboxId: Long) {
        messageDao.deleteOne(mailboxId, messageId)
    }

    suspend fun unreadCount(mailboxId: Long): Int = messageDao.unreadCount(mailboxId)
}