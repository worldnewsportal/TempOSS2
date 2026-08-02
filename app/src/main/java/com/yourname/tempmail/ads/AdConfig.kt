package com.yourname.tempmail.ads

import com.yourname.tempmail.BuildConfig

/**
 * All Unity/LevelPlay identifiers. Values are injected from `local.properties`
 * via BuildConfig so secrets never live in the repository (rule #35).
 *
 * Debug builds intentionally use test/demo IDs; release builds require the real
 * IDs from the developer's LevelPlay dashboard.
 */
object AdConfig {
    val appKey: String = BuildConfig.UNITY_APP_KEY
    val bannerAdUnitId: String = BuildConfig.UNITY_BANNER_AD_UNIT
    val interstitialAdUnitId: String = BuildConfig.UNITY_INTERSTITIAL_AD_UNIT
    val rewardedAdUnitId: String = BuildConfig.UNITY_REWARDED_AD_UNIT

    val testMode: Boolean = BuildConfig.IS_TEST_MODE

    val isConfigured: Boolean
        get() = appKey.isNotBlank() &&
            bannerAdUnitId != "demoBanner" &&
            interstitialAdUnitId != "demoInterstitial" &&
            rewardedAdUnitId != "demoRewarded" &&
            bannerAdUnitId != "YOUR_BANNER_AD_UNIT_ID"
}

/** Ad reward products (optional, unlocked only via rewarded ads). */
enum class RewardId(val id: String) {
    EXTRA_SLOT("extra_slot"),
    ORGANIZATION("organization"),
    PREMIUM_THEME("premium_theme"),
}