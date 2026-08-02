package com.yourname.tempmail

import com.yourname.tempmail.security.HtmlSanitizer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlSanitizerTest {

    private val sanitizer = HtmlSanitizer()

    @Test
    fun `script tag stripped`() {
        val out = sanitizer.sanitize("<p>hi</p><script>alert(1)</script>")!!
        assertFalse(out.contains("script"))
        assertTrue(out.contains("hi"))
    }

    @Test
    fun `event handler stripped`() {
        val out = sanitizer.sanitize("<img src=x onerror=alert(1)>")!!
        assertFalse(out.contains("onerror"))
    }

    @Test
    fun `javascript href removed`() {
        val out = sanitizer.sanitize("<a href=javascript:alert(1)>x</a>")!!
        assertFalse(out.contains("javascript"))
    }

    @Test
    fun `link kept`() {
        val out = sanitizer.sanitize("<a href=\"https://example.com\">ok</a>")!!
        assertTrue(out.contains("https://example.com"))
    }

    @Test
    fun `blank returns null`() {
        assertTrue(sanitizer.sanitize(null) == null)
        assertTrue(sanitizer.sanitize("") == null)
        assertTrue(sanitizer.sanitize("   ") == null)
    }

    @Test
    fun `stripToText removes markup`() {
        val t = sanitizer.stripToText("<b>Hello</b> <i>world</i>")
        assertTrue(t.contains("Hello"))
        assertTrue(t.contains("world"))
        assertFalse(t.contains("<"))
    }
}