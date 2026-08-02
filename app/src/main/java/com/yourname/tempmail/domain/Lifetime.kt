package com.yourname.tempmail.domain

/** 7-day expiry math shared across the app and tests. */
object Lifetime {
    const val MILLIS_PER_DAY = 86_400_000L

    fun of(ttlHours: Int): Long = ttlHours * 3_600_000L

    fun remaining(expiresAt: Long, now: Long = System.currentTimeMillis()): Long =
        (expiresAt - now).coerceAtLeast(0L)

    fun isExpired(mailbox: Mailbox, now: Long = System.currentTimeMillis()): Boolean =
        mailbox.expiresAt <= now

    /** "7 days remaining", "6 days 23 hours remaining", "12 hours 3 min remaining". */
    fun humanize(expiresAt: Long, now: Long = System.currentTimeMillis()): String {
        val ms = remaining(expiresAt, now)
        if (ms <= 0) return "expired"
        val totalMinutes = ms / 60_000L
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60
        return when {
            days > 0 -> "$days days ${hours}h"
            hours > 0 -> "$hours hours ${minutes}min"
            else -> "$minutes minutes"
        }
    }
}