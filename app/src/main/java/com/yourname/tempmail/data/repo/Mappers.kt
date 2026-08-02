package com.yourname.tempmail.data.repo

import com.yourname.tempmail.data.db.MailboxEntity
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.domain.Lifetime
import com.yourname.tempmail.domain.Mailbox

fun MailboxEntity.toDomain(): Mailbox = Mailbox(
    id = id,
    providerId = providerId,
    email = com.yourname.tempmail.domain.EmailAddress(login, domain),
    createdAt = createdAt,
    expiresAt = expiresAt,
    displayName = displayName,
    favorite = favorite,
    unreadCount = unreadCount,
    lastSyncedAt = lastSyncedAt,
)

fun Mailbox.toEntity(): MailboxEntity = MailboxEntity(
    id = id,
    providerId = providerId,
    login = email.login,
    domain = email.domain,
    email = email.full,
    createdAt = createdAt,
    expiresAt = expiresAt,
    displayName = displayName,
    favorite = favorite,
    unreadCount = unreadCount,
    lastSyncedAt = lastSyncedAt,
)

/** Convenience so callers never hold a DB handle to purge expired mailboxes. */
suspend fun TempmailDatabase.purgeExpiredMailboxes(now: Long): List<Long> {
    val expired = mailboxDao().all().filter { it.expiresAt <= now }.map { it.id }
    if (expired.isNotEmpty()) {
        messageDao().deleteForMailboxIds(expired)
        attachmentDao().deleteForMailboxIds(expired)
        mailboxDao().deleteByIds(expired)
    }
    return expired
}