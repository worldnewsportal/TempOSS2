package com.yourname.tempmail.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourname.tempmail.data.db.MessageEntity
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.Lifetime
import com.yourname.tempmail.domain.Mailbox
import com.yourname.tempmail.domain.ProviderResult
import com.yourname.tempmail.providers.EmailProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

/**
 * Single app-scope ViewModel. Holds the common UI state (theme, locale, ads
 * consent) and the navigation "current mailbox". Per-screen concerns live in the
 * screens themselves via the repositories from [AppContainer].
 */
class TempmailAppViewModel(
    private val container: AppContainer,
) : ViewModel() {

    val themeMode: StateFlow<String> = container.settings.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val locale: StateFlow<String> = container.settings.locale
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    val adsConsentShown: StateFlow<Boolean> = container.settings.adsConsentShown
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val onboardingDone: StateFlow<Boolean> = container.settings.onboardingDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _currentMailbox = MutableStateFlow<Mailbox?>(null)
    val currentMailbox: StateFlow<Mailbox?> = _currentMailbox.asStateFlow()

    private val _events = MutableStateFlow<UiEvent?>(null)
    val events: StateFlow<UiEvent?> = _events.asStateFlow()

    private val _copied = MutableStateFlow(false)
    val copied: StateFlow<Boolean> = _copied.asStateFlow()

    fun setTheme(mode: String) {
        viewModelScope.launch { container.settings.setTheme(mode) }
    }

    fun setLocale(code: String) {
        viewModelScope.launch {
            container.settings.setLocale(code)
            consumeEvent(UiEvent.RestartActivity)
        }
    }

    fun openMailbox(mailbox: Mailbox) { _currentMailbox.value = mailbox }

    fun clearCurrentMailbox() { _currentMailbox.value = null }

    fun enterMailbox(mailbox: Mailbox) {
        _currentMailbox.value = mailbox
        emit(UiEvent.NavigateMailbox(mailbox.id))
    }

    fun emit(event: UiEvent) {
        if (event is UiEvent.NavigateMailboxList) _currentMailbox.value = null
        _events.value = event
    }

    fun consumeEvent() { _events.value = null }

    fun consumeEvent(event: UiEvent) { if (_events.value === event) _events.value = null }

    fun onCopied(show: Boolean) { _copied.value = show }

    fun setAdsConsent(accepted: Boolean, hasReviewed: Boolean) {
        viewModelScope.launch {
            container.settings.setAdsEnabled(accepted)
            container.settings.setAdsConsentShown(hasReviewed)
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch { container.settings.setOnboardingDone(true) }
    }

    fun copyToClipboard(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("address", text))
        _copied.value = true
    }
}

sealed interface UiEvent {
    data object RestartActivity : UiEvent
    data object NavigateMailboxList : UiEvent
    data object NavigateCreate : UiEvent
    data object NavigateSettings : UiEvent
    data object NavigateBack : UiEvent
    data class NavigateMailbox(val id: Long) : UiEvent
    data class NavigateMessage(val mailboxId: Long, val messageId: Long) : UiEvent
    data class ShowSnackbar(val message: String) : UiEvent
}

class TempmailAppViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TempmailAppViewModel(container) as T
}