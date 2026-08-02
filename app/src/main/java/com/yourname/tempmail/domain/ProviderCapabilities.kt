package com.yourname.tempmail.domain

/** What a temporary-mail provider really guarantees. Never promises more. */
data class ProviderCapabilities(
    val id: String,
    val displayName: String,
    val supportsReceiving: Boolean = true,
    val supportsSending: Boolean = false,
    val supportsReply: Boolean = false,
    val supportsForward: Boolean = false,
    val supportsAttachments: Boolean = false,
    val supportsCustomUsername: Boolean = false,
    val supportsMultipleDomains: Boolean = false,
    val supportsRealtime: Boolean = false,
    val supportsSearch: Boolean = false,
    val authRequired: Boolean = false,
    val mailboxTtlHours: Int = 168, // most temp providers store ~7 days
) {
    companion object {
        val ONESECMAIL = ProviderCapabilities(
            id = "onesecmail",
            displayName = "1secmail",
            supportsAttachments = true,
            supportsCustomUsername = true,
            supportsMultipleDomains = true,
            supportsRealtime = true,
            supportsSearch = false,
            mailboxTtlHours = 168,
        )

        val MAIL_TM = ProviderCapabilities(
            id = "mailtm",
            displayName = "mail.tm",
            supportsAttachments = true,
            supportsCustomUsername = true,
            supportsMultipleDomains = true,
            supportsRealtime = true,
            supportsSearch = true,
            authRequired = true,
            mailboxTtlHours = 168,
        )
    }
}