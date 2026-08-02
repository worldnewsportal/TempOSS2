package com.yourname.tempmail

import com.yourname.tempmail.domain.ProviderCapabilities
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderCapabilitiesTest {

    @Test
    fun `onesecmail advertises no sending`() {
        val c = ProviderCapabilities.ONESECMAIL
        assertFalse(c.supportsSending)
        assertFalse(c.supportsReply)
        assertTrue(c.supportsReceiving)
        assertTrue(c.supportsCustomUsername)
        assertTrue(c.supportsMultipleDomains)
        assertTrue(c.supportsRealtime)
        assertEquals(168, c.mailboxTtlHours)
    }

    @Test
    fun `mailtm advertises no sending`() {
        val c = ProviderCapabilities.MAIL_TM
        assertFalse(c.supportsSending)
        assertFalse(c.supportsReply)
        assertTrue(c.supportsReceiving)
        assertTrue(c.supportsSearch)
        assertTrue(c.authRequired)
        assertEquals(168, c.mailboxTtlHours)
    }

    @Test
    fun `default capabilities only receiving`() {
        val c = ProviderCapabilities(id = "x", displayName = "x")
        assertTrue(c.supportsReceiving)
        assertFalse(c.supportsSending)
        assertFalse(c.supportsAttachments)
        assertEquals(168, c.mailboxTtlHours)
    }

    private fun assertEquals(expected: Any?, actual: Any?) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}