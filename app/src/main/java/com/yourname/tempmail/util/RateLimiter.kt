package com.yourname.tempmail.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Very light client-side throttle so we never hammer a provider API (rule #21).
 * Not an anti-abuse control — just politeness.
 */
class RateLimiter(
    private val minIntervalMs: Long = 2_000L,
) {
    private val lastHit = ConcurrentHashMap<String, AtomicLong>()

    fun allow(key: String): Boolean {
        val now = System.currentTimeMillis()
        val prev = lastHit[key] ?: AtomicLong(0)
        val before = prev.get()
        if (now - before < minIntervalMs) return false
        return prev.compareAndSet(before, now)
    }

    fun <T> run(key: String, block: () -> T): T? =
        if (allow(key)) block() else null
}