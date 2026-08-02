package com.yourname.tempmail.security

import android.net.Uri
import java.net.URI

/** Strict URL validation used before opening any external link from e-mail HTML. */
object UrlValidator {

    private val ALLOWED = setOf("http", "https", "mailto", "tel")

    fun safeToOpen(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val candidate = raw.trim()
        val parsed = runCatching { URI(candidate) }.getOrNull() ?: return null
        val scheme = parsed.scheme?.lowercase() ?: return null
        if (scheme !in ALLOWED) return null
        // Reject any attempt to smuggle scripts via percent-encoding inside the path.
        if (parsed.rawPath?.contains('%', ignoreCase = true) == true) {
            val decoded = runCatching { Uri.decode(parsed.rawPath) }.getOrNull() ?: return null
            if (decoded.contains("javascript:", ignoreCase = true)) return null
        }
        return candidate
    }
}