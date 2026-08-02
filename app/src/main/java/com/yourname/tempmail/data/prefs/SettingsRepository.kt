package com.yourname.tempmail.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tempmail-preferences")

/** Typed user preferences. No secrets live here (see SecretStore). */
class SettingsRepository(private val context: Context) {

    object Keys {
        val locale = stringPreferencesKey("locale")
        val theme = stringPreferencesKey("theme")
        val autoRefreshMs = longPreferencesKey("auto_refresh_ms")
        val notifications = booleanPreferencesKey("notifications_enabled")
        val notifSound = booleanPreferencesKey("notif_sound")
        val notifVibrate = booleanPreferencesKey("notif_vibrate")
        val notifPreview = booleanPreferencesKey("notif_preview")
        val adsEnabled = booleanPreferencesKey("ads_enabled")
        val adsConsentShown = booleanPreferencesKey("ads_consent_shown")
        val interstitialCooldownActions = intPreferencesKey("interstitial_cooldown_actions")
        val onboardingDone = booleanPreferencesKey("onboarding_done")
    }

    val locale: Flow<String> = data(Keys.locale, "en")
    val theme: Flow<String> = data(Keys.theme, "system")
    val autoRefreshInterval: Flow<Long> = data(Keys.autoRefreshMs, 60_000L)
    val notifications: Flow<Boolean> = data(Keys.notifications, true)
    val notifSound: Flow<Boolean> = data(Keys.notifSound, true)
    val notifVibrate: Flow<Boolean> = data(Keys.notifVibrate, true)
    val notifPreview: Flow<Boolean> = data(Keys.notifPreview, true)
    val adsEnabled: Flow<Boolean> = data(Keys.adsEnabled, false)
    val adsConsentShown: Flow<Boolean> = data(Keys.adsConsentShown, false)
    val interstitialCooldownActions: Flow<Int> = data(Keys.interstitialCooldownActions, 3)
    val onboardingDone: Flow<Boolean> = data(Keys.onboardingDone, false)

    private inline fun <reified T> data(key: Preferences.Key<T>, def: T): Flow<T> =
        context.dataStore.data.map { it[key] ?: def }

    suspend fun setLocale(v: String) = set(Keys.locale, v)
    suspend fun setTheme(v: String) = set(Keys.theme, v)
    suspend fun setAutoRefresh(v: Long) = set(Keys.autoRefreshMs, v)
    suspend fun setNotifications(v: Boolean) = set(Keys.notifications, v)
    suspend fun setNotifSound(v: Boolean) = set(Keys.notifSound, v)
    suspend fun setNotifVibrate(v: Boolean) = set(Keys.notifVibrate, v)
    suspend fun setNotifPreview(v: Boolean) = set(Keys.notifPreview, v)
    suspend fun setAdsEnabled(v: Boolean) = set(Keys.adsEnabled, v)
    suspend fun setAdsConsentShown(v: Boolean) = set(Keys.adsConsentShown, v)
    suspend fun setInterstitialCooldownActions(v: Int) = set(Keys.interstitialCooldownActions, v)
    suspend fun setOnboardingDone(v: Boolean) = set(Keys.onboardingDone, v)

    private suspend inline fun <reified T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}