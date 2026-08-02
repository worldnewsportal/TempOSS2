package com.yourname.tempmail

import com.yourname.tempmail.util.RateLimiter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLimiterTest {

    @Test
    fun `first hit allowed`() {
        assertTrue(RateLimiter(10_000).allow("key"))
    }

    @Test
    fun `immediate second hit blocked`() {
        val rl = RateLimiter(50_000)
        assertTrue(rl.allow("key"))
        assertFalse(rl.allow("key"))
    }

    @Test
    fun `different keys independent`() {
        val rl = RateLimiter(50_000)
        assertTrue(rl.allow("a"))
        assertTrue(rl.allow("b"))
    }

    @Test
    fun `run returns block result when allowed`() {
        val rl = RateLimiter(50_000)
        assertEquals(42, rl.run("k") { 42 })
    }

    @Test
    fun `run returns null when throttled`() {
        val rl = RateLimiter(50_000)
        rl.run("k") { 1 }
        assertNull(rl.run("k") { 2 })
    }
}