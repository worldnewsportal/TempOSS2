package com.yourname.tempmail

import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.Lifetime
import com.yourname.tempmail.domain.Mailbox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifetimeTest {

    private val now = 1_700_000_000_000L

    private fun mailbox(createdAt: Long, expiresAt: Long? = null): Mailbox = Mailbox(
        providerId = "onesecmail",
        email = EmailAddress("user1", "example.com"),
        createdAt = createdAt,
        expiresAt = expiresAt ?: (createdAt + Lifetime.of(168)),
    )

    @Test
    fun `7 days is 168h`() {
        assertEquals(168L, Lifetime.of(168) / 3_600_000L)
    }

    @Test
    fun `fresh mailbox is not expired`() {
        val m = mailbox(createdAt = now - 60_000L)
        assertFalse(Lifetime.isExpired(m, now))
    }

    @Test
    fun `mailbox past 7 days is expired`() {
        val m = mailbox(createdAt = now - 169 * 3_600_000L)
        assertTrue(Lifetime.isExpired(m, now))
    }

    @Test
    fun `expiresAt equals createdAt plus ttl`() {
        val m = mailbox(createdAt = 1_000L)
        assertEquals(1_000L + 168 * 3_600_000L, m.expiresAt)
    }

    @Test
    fun `humanize gives non-empty remaining text`() {
        val m = mailbox(createdAt = now - 3_600_000L)
        val text = Lifetime.humanize(m.expiresAt, now)
        assertTrue(text.isNotBlank())
    }

    @Test
    fun `humanize of expired returns expired`() {
        assertEquals("expired", Lifetime.humanize(now - 1L, now))
    }
}