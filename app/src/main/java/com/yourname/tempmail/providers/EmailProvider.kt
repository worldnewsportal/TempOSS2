package com.yourname.tempmail.providers

import com.yourname.tempmail.domain.Contact
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.EmailMessage
import com.yourname.tempmail.domain.AttachmentRef
import com.yourname.tempmail.domain.Mailbox
import com.yourname.tempmail.domain.ProviderCapabilities
import com.yourname.tempmail.domain.ProviderName
import com.yourname.tempmail.domain.ProviderResult
import kotlinx.coroutines.flow.Flow

/** A minimized, list-view representation of a message. */
data class MessageThumb(
    val providerRawId: String,
    val from: Contact = Contact(),
    val subject: String = "",
    val preview: String = "",
    val date: Long = 0L,
    val hasAttachments: Boolean = false,
)

/**
 * Contract every provider implements. Capabilities are explicit so the UI can
 * truthfully enable/disable send, reply, attachments, realtime, etc. — and never
 * pretend a feature exists when its provider doesn't support it (rule #60).
 */
interface EmailProvider {
    val id: String
    val displayName: String

    /** Human description of exactly what this provider supports. */
    val capabilities: ProviderCapabilities

    /** Cheap liveness probe (head / tiny request). */
    suspend fun health(): ProviderHealth

    /** Domains that can actually receive mail right now. */
    suspend fun domains(): ProviderResult<List<String>>

    /** Generate [count] random addresses the provider accepts. */
    suspend fun generate(randomCount: Int): ProviderResult<List<EmailAddress>>

    /** Reserve/create a mailbox for the given address; persist any auth state. */
    suspend fun createMailbox(address: EmailAddress): ProviderResult<Unit>

    /** List message summaries for a mailbox (no bodies). */
    suspend fun listMessages(mailbox: Mailbox): ProviderResult<List<MessageThumb>>

    /** Fetch full message (body + headers + attachments metadata). */
    suspend fun readMessage(mailbox: Mailbox, messageId: String): ProviderResult<EmailMessage>

    /** Mark seen/unseen if the provider supports it. */
    suspend fun markSeen(mailbox: Mailbox, messageId: String, seen: Boolean): ProviderResult<Unit>

    /** Download real attachment bytes when the provider supports it. */
    suspend fun attachmentBytes(
        mailbox: Mailbox,
        messageId: String,
        attachment: AttachmentRef,
    ): ProviderResult<ByteArray>

    /** Real-time push of new-message notification strings (null if unsupported). */
    fun messageEvents(mailbox: Mailbox): Flow<String>?
}

data class ProviderHealth(
    val status: ProviderName = ProviderName.UNKNOWN,
    val latencyMs: Long = -1,
    val message: String? = null,
)