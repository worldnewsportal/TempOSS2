package com.yourname.tempmail.domain

/**
 * A real, usable e-mail identity: user@domain.
 *
 * Always split and re-built from its parts so that localized UI operations work
 * on stable units (login vs. domain) even when the user changes language.
 * Email addresses and domain names are never translated.
 */
data class EmailAddress(
    val login: String,
    val domain: String,
) {
    val full: String get() = "$login@$domain"

    companion object {
        private val LOCAL_PART = Regex("^[A-Za-z0-9.\\-_+]{3,64}$")

        fun from(fullAddress: String): EmailAddress? {
            val at = fullAddress.lastIndexOf('@')
            if (at <= 0 || at >= fullAddress.length - 1) return null
            val login = fullAddress.substring(0, at)
            val domain = fullAddress.substring(at + 1)
            if (!LOCAL_PART.matches(login)) return null
            return EmailAddress(login, domain)
        }
    }
}