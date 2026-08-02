package com.yourname.tempmail

import com.yourname.tempmail.security.UrlValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlValidatorTest {

    @Test
    fun `http allowed`() {
        assertEquals("https://example.com/x", UrlValidator.safeToOpen("https://example.com/x"))
    }

    @Test
    fun `mailto allowed`() {
        assertEquals("mailto:user@example.com", UrlValidator.safeToOpen("mailto:user@example.com"))
    }

    @Test
    fun `tel allowed`() {
        assertEquals("tel:+1000", UrlValidator.safeToOpen("tel:+1000"))
    }

    @Test
    fun `javascript blocked`() {
        assertNull(UrlValidator.safeToOpen("javascript:alert(1)"))
    }

    @Test
    fun `vbscript blocked`() {
        assertNull(UrlValidator.safeToOpen("vbscript:msgbox(1)"))
    }

    @Test
    fun `no scheme blocked`() {
        assertNull(UrlValidator.safeToOpen("/etc/passwd"))
    }

    @Test
    fun `ftp blocked`() {
        assertNull(UrlValidator.safeToOpen("ftp://example.com"))
    }

    @Test
    fun `blank blocked`() {
        assertNull(UrlValidator.safeToOpen("  "))
        assertNull(UrlValidator.safeToOpen(null))
    }

    @Test
    fun `percent-encoded javascript blocked`() {
        assertNull(UrlValidator.safeToOpen("https://example.com/%6A%61%76%61%73%63%72%69%70%74%3A"))
    }
}