package com.yourname.tempmail.ui.settings

/** Static UI config values shared by settings & theme wiring. */
object SettingsUiState {
    const val themeDefaultKey = "system"
    const val localeDefaultKey = "en"

    val autoRefreshOptions = listOf(
        "manual" to 0L,
        "10s" to 10_000L,
        "30s" to 30_000L,
        "1m" to 60_000L,
        "5m" to 300_000L,
        "10m" to 600_000L,
    )
}