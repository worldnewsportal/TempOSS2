package com.yourname.tempmail.providers

import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.ProviderResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Facade callers use. Routes to the correct [EmailProvider] and, inside,
 * handles provider switching/fallback when a provider is down.
 */
class ProviderManager(
    private val registry: ProviderRegistry,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _currentProvider = MutableStateFlow<EmailProvider?>(null)
    val currentProvider: StateFlow<EmailProvider?> = _currentProvider

    init {
        scope.launch {
            registry.refreshHealth()
            _currentProvider.value = registry.usable().firstOrNull()
        }
    }

    fun providerFor(id: String): EmailProvider? = registry.byId(id)

    suspend fun refreshHealth() { registry.refreshHealth() }

    fun refresh() = scope.launch {
        registry.refreshHealth()
        _currentProvider.value = registry.usable().firstOrNull()
    }

    /** Cancel internal coroutines. Call from Application.onTerminate(). */
    fun cancel() {
        scope.cancel()
    }

    suspend fun domainsFor(id: String): ProviderResult<List<String>> =
        registry.byId(id)?.domains() ?: ProviderResult.Failure("provider.missing")

    suspend fun generateFor(id: String, count: Int): ProviderResult<List<EmailAddress>> =
        registry.byId(id)?.generate(count) ?: ProviderResult.Failure("provider.missing")

    suspend fun createMailboxFor(id: String, address: EmailAddress): ProviderResult<Unit> =
        registry.byId(id)?.createMailbox(address) ?: ProviderResult.Failure("provider.missing")

    fun realtimeFor(id: String, mailbox: com.yourname.tempmail.domain.Mailbox): Flow<String>? =
        registry.byId(id)?.messageEvents(mailbox)
}