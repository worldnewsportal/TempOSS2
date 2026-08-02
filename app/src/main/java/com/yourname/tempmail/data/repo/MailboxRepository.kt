package com.yourname.tempmail.data.repo

import com.yourname.tempmail.data.db.MailboxEntity
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.data.daos.MailboxDao
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.Lifetime
import com.yourname.tempmail.domain.Mailbox
import com.yourname.tempmail.domain.ProviderResult
import com.yourname.tempmail.providers.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single app facade over providers + storage for mailbox lifecycle.
 *
 * Rules enforced here (rules #1, #9, #10):
 *  - 7-day lifetime is derived from `createdAt`.
 *  - Change/Generate works immediately — no cooldown is stored anywhere.
 *  - Expired mailboxes are purged with their cached message data.
 */
class MailboxRepository(
    private val db: TempmailDatabase,
    private val providers: ProviderManager,
) {
    private val mailboxDao: MailboxDao = db.mailboxDao()

    fun observeActive(): Flow<List<Mailbox>> =
        mailboxDao.observeAll().map { list -> list.map { it.toDomain() }.filter { !Lifetime.isExpired(it) } }

    fun observeAllIncludingExpired(): Flow<List<Mailbox>> =
        mailboxDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun create(
        providerId: String,
        address: EmailAddress,
        customName: String = "",
    ): Result<Mailbox> {
        val provider = providers.providerFor(providerId)
            ?: return Result.failure(IllegalStateException("provider_missing"))
        val created = provider.createMailbox(address)
        if (created is ProviderResult.Failure) {
            return Result.failure(IllegalStateException(created.reason))
        }
        val now = System.currentTimeMillis()
        val ttl = Lifetime.of(provider.capabilities.mailboxTtlHours)
        val mailbox = Mailbox(
            providerId = providerId,
            email = address,
            createdAt = now,
            expiresAt = now + ttl,
            displayName = customName.ifBlank { address.full },
        )
        val id = mailboxDao.insert(mailbox.toEntity())
        return Result.success(mailbox.copy(id = id))
    }

    suspend fun get(id: Long): Mailbox? = mailboxDao.byId(id)?.toDomain()

    suspend fun delete(id: Long) {
        db.messageDao().deleteForMailbox(id)
        db.attachmentDao().deleteForMailbox(id)
        mailboxDao.deleteById(id)
    }

    suspend fun deleteAll() {
        db.messageDao().purgeAll()
        db.attachmentDao().purgeAttachments()
        mailboxDao.deleteAll()
    }

    suspend fun rename(id: Long, newName: String) {
        val e = mailboxDao.byId(id) ?: return
        mailboxDao.update(e.copy(displayName = newName))
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) {
        val e = mailboxDao.byId(id) ?: return
        mailboxDao.update(e.copy(favorite = favorite))
    }

    suspend fun touchSynced(id: Long, unread: Int, at: Long = System.currentTimeMillis()) {
        val e = mailboxDao.byId(id) ?: return
        mailboxDao.update(e.copy(lastSyncedAt = at, unreadCount = unread))
    }

    /** Remove any mailbox whose expiry passed; returns removed ids. */
    suspend fun purgeExpired(now: Long = System.currentTimeMillis()): List<Long> =
        db.purgeExpiredMailboxes(now)

}