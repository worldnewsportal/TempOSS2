package com.yourname.tempmail.domain

import java.io.File

/**
 * A message as fetched from a provider (summary or full).
 */
data class EmailMessage(
    val id: String = "",
    val mailboxId: String = "",
    val providerId: String = "",
    val from: Contact = Contact(),
    val to: List<Contact> = emptyList(),
    val cc: List<Contact> = emptyList(),
    val bcc: List<Contact> = emptyList(),
    val subject: String = "",
    val preview: String = "",
    val bodyHtml: String? = null,
    val bodyText: String? = null,
    val date: Long = 0L,
    val seen: Boolean = false,
    val starred: Boolean = false,
    val label: String? = null, // e.g. INBOX, SPAM, TRASH, ARCHIVE
    val attachments: List<AttachmentRef> = emptyList(),
    val messageIdHeader: String? = null,
    val inReplyTo: String? = null,
    val threadingKey: String? = null,
    val providerRawId: String = "",
)

data class Contact(val name: String = "", val address: String = "") {
    val display: String get() = if (name.isBlank()) address else "$name <$address>"
}

data class AttachmentRef(
    val providerMessageId: String = "",
    val filename: String = "",
    val sizeBytes: Long = 0,
    val contentDisposition: String? = null,
    val url: String? = null,
    val provider: String = "",
)

data class DraftInfo(
    val id: Long = 0,
    val mailboxId: String = "",
    val to: String = "",
    val cc: String = "",
    val bcc: String = "",
    val subject: String = "",
    val body: String = "",
    val updatedAt: Long = 0,
)

data class ProviderHealthInfo(
    val providerId: String,
    val status: ProviderName,
    val message: String? = null,
)