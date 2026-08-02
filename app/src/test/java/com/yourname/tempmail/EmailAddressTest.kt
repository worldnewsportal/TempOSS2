package com.yourname.tempmail

import com.yourname.tempmail.domain.EmailAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailAddressTest {

    @Test
    fun `valid address parses`() {
        val a = EmailAddress.from("joe.doe-2024@example.com")
        assertNotNull(a)
        assertEquals("joe.doe-2024", a!!.login)
        assertEquals("example.com", a.domain)
        assertEquals("joe.doe-2024@example.com", a.full)
    }

    @Test
    fun `uppercase login allowed`() {
        val a = EmailAddress.from("JohnB@example.com")
        assertNotNull(a)
        assertEquals("JohnB", a!!.login)
    }

    @Test
    fun `rejects missing at`() {
        assertNull(EmailAddress.from("not-an-email"))
    }

    @Test
    fun `rejects empty domain`() {
        assertNull(EmailAddress.from("user@"))
    }

    @Test
    fun `rejects empty login`() {
        assertNull(EmailAddress.from("@example.com"))
    }

    @Test
    fun `rejects too-short login`() {
        assertNull(EmailAddress.from("ab@example.com"))
    }

    @Test
    fun `rejects illegal characters`() {
        assertNull(EmailAddress.from("us er@example.com"))
        assertNull(EmailAddress.from("us@er!@example.com"))
    }

    @Test
    fun `uses last at as separator`() {
        val a = EmailAddress.from("display+tag@sub.example.co")
        assertNotNull(a)
        assertEquals("sub.example.co", a!!.domain)
    }
}