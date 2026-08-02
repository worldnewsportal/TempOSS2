package com.yourname.tempmail.providers

import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.ProviderCapabilities
import com.yourname.tempmail.domain.ProviderName
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry + health tracking for all providers. Registered providers are probed
 * lazily; unhealthy providers are excluded from the domain picker so we never
 * display a domain that can't actually be used (rule #6).
 */
class ProviderRegistry(
    private val providers: List<EmailProvider>,
) {
    private val healthCache = ConcurrentHashMap<String, ProviderHealth>()

    fun all(): List<EmailProvider> = providers.toList()

    fun byId(id: String): EmailProvider? = providers.firstOrNull { it.id == id }

    /** Providers currently usable (unknown counts as usable until proven DOWN). */
    fun usable(): List<EmailProvider> = providers.filter { healthOf(it.id).status != ProviderName.DOWN }

    fun healthOf(providerId: String): ProviderHealth =
        healthCache[providerId] ?: ProviderHealth()

    fun capabilitiesOf(providerId: String): ProviderCapabilities = byId(providerId)?.capabilities
        ?: ProviderCapabilities(id = providerId, displayName = providerId)

    /** Refresh all provider health; returns snapshot. */
    suspend fun refreshHealth(): Map<String, ProviderHealth> {
        providers.forEach { p ->
            val health = runCatching { p.health() }.getOrElse { ProviderHealth(ProviderName.DOWN, message = "$it") }
            healthCache[p.id] = health
        }
        return healthCache.toMap()
    }

    /** True if any usable provider supports the given feature. */
    fun anySupportsSending() = usable().any { it.capabilities.supportsSending }
}