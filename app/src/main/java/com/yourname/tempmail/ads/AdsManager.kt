package com.yourname.tempmail.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central LevelPlay umbrella. The ad subsystem is fully isolated: if it fails or
 * is not configured, the e-mail app keeps working (rule #27).
 *
 * Debug vs release behaviour (test vs production IDs) is driven entirely by
 * BuildConfig (rule #34). In release you must supply real LevelPlay credentials
 * in `local.properties`, otherwise ads disable themselves gracefully.
 */
class AdsManager(
    private val context: Context,
    private val db: TempmailDatabase,
    private val settings: SettingsRepository,
) {
    private val _initialized = MutableStateFlow(false)
    val initialized: StateFlow<Boolean> = _initialized.asStateFlow()

    val rewardManager = RewardManager(db.adRewardDao())

    private val session: LevelPlaySession = LevelPlaySessionFactory.create(context)

    val banner: BannerAdManager = BannerAdManager(session)
    val interstitial: InterstitialAdManager = InterstitialAdManager(context, session)
    val rewarded: RewardedAdManager = RewardedAdManager(context, rewardManager, session)

    /** Call once from Application.onCreate. */
    fun initialize() {
        if (!AdConfig.isConfigured) {
            Log.i(TAG, "Ads not configured (set Unity/LevelPlay IDs in local.properties for release).")
            banner.markDisabled()
            interstitial.markDisabled()
            rewarded.markDisabled()
            _initialized.value = false
            return
        }
        session.initialize(AdConfig.appKey, object : LevelPlayInitListener {
            override fun onInitSuccess() {
                _initialized.value = true
                interstitial.preload()
                rewarded.preload()
            }

            override fun onInitFailure(errorMessage: String?) {
                Log.w(TAG, "Ads init failed: $errorMessage")
                _initialized.value = false
                banner.markDisabled()
                interstitial.markDisabled()
                rewarded.markDisabled()
            }
        })
    }

    /** Offers an interstitial at a natural navigation point (rule #29). */
    fun maybeShowInterstitial(activity: Activity?) {
        if (_initialized.value) interstitial.maybeShow(activity)
    }

    fun onActivityResume(activity: Activity) { interstitial.onResume(activity) }
    fun onActivityPause() { interstitial.onPause() }
    fun onActivityDestroy() { interstitial.onDestroy(); banner.destroy() }

    companion object { private const val TAG = "AdsManager" }
}