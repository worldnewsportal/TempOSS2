package com.yourname.tempmail.domain

/**
 * A disposable mailbox held by the user. One mailbox == one active address.
 *
 * Mailboxes live exactly 7 days from creation; `expiresAt` is stored and the
 * app stops syncing + deletes local cached data the moment it passes.
 */
data class Mailbox(
    val id: Long = 0,
    val providerId: String,
    val email: EmailAddress,
    val createdAt: Long,
    val expiresAt: Long,
    val displayName: String = "",
    val favorite: Boolean = false,
    val unreadCount: Int = 0,
    val lastSyncedAt: Long = 0L,
)

/** Metadata a provider returns (or user supplies) when creating a mailbox. */
data class MailboxCredentials(
    val secretRef: String? = null, // key into the Keystore-backed store (mail.tm JWT / password)
    val mailboxId: String? = null, // provider-side account id
)

sealed interface ProviderResult<out T> {
    data class Success<T>(val data: T) : ProviderResult<T>
    data class Failure(val reason: String, val cause: Exception? = null) : ProviderResult<Nothing>
}