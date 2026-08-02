package com.yourname.tempmail.ads

import android.content.Context

/**
 * SOLE point of contact with the LevelPlay / ironSource SDK.
 *
 * The rest of the app talks to [LevelPlaySession] behind callbacks, so verifying
 * the installed `mediation-sdk` AAR requires touching one file. The SDK surface
 * below follows the current (9.x) LevelPlay Android integration: "App Key" init
 * with a listener, ad objects created per ad-unit, explicit load + show.
 */
interface LevelPlaySession {

    /** Real init. `appKey` comes from BuildConfig (local.properties). */
    fun initialize(
        appKey: String,
        listener: LevelPlayInitListener,
    )

    /** Banner. Returns a handle; SDK mounts its own view. */
    fun createBanner(adUnitId: String, listener: BannerListener): Any?

    /** Interstitial: begins a background load of `adUnitId`. */
    fun loadInterstitial(adUnitId: String, listener: FullscreenListener)

    fun isInterstitialReady(): Boolean

    fun showInterstitial(context: Context, placement: String)

    /** Rewarded: begins a background load of `adUnitId`. */
    fun loadRewarded(adUnitId: String, listener: RewardedListener)

    fun isRewardedReady(): Boolean

    fun showRewarded(context: Context)

    /** True only when the SDK has finished initialization. */
    fun isInitialized(): Boolean
}

interface LevelPlayInitListener {
    fun onInitSuccess()
    fun onInitFailure(errorMessage: String?)
}

interface FullscreenListener {
    fun onLoaded()
    fun onLoadFailed(error: String?)
    fun onDisplayed()
    fun onFailedToDisplay(error: String?)
    fun onClosed()
}

interface RewardedListener : FullscreenListener {
    /** Called ONLY by the SDK after a rewarded video completes successfully. */
    fun onRewarded()
    fun onAdClicked()
}

interface BannerListener {
    fun onAdLoaded()
    fun onAdLoadFailed(error: String?)
    fun onAdDisplayed()
}