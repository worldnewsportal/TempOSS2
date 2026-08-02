package com.yourname.tempmail.ads

import android.app.Activity
import android.content.Context
import android.view.View
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Banner ads. Mounted only on Home / MailboxManager in a dedicated slot; never
 * over e-mail bodies, compose fields, important buttons, or navigation (rule #28).
 */
class BannerAdManager(
    private val session: LevelPlaySession = Sessions.current(),
) {
    private val _state = MutableStateFlow(AdState(AdAvailabilityState.NOT_CONFIGURED))
    val state: StateFlow<AdState> = _state.asStateFlow()

    private var bannerView: View? = null

    val currentView: View? get() = bannerView

    private val listener = object : BannerListener {
        override fun onAdLoaded() { _state.value = AdState(AdAvailabilityState.AVAILABLE) }
        override fun onAdLoadFailed(error: String?) { _state.value = AdState(AdAvailabilityState.FAILED) }
        override fun onAdDisplayed() { _state.value = AdState(AdAvailabilityState.AVAILABLE) }
    }

    /** Load & mount a real banner; returns the view or null when not configured. */
    fun load(activity: Activity): View? {
        if (!AdConfig.isConfigured) {
            _state.value = AdState(AdAvailabilityState.NOT_CONFIGURED)
            return null
        }
        _state.value = AdState(AdAvailabilityState.LOADING)
        val created = session.createBanner(AdConfig.bannerAdUnitId, listener)
        if (created is View) {
            bannerView = created
            _state.value = AdState(AdAvailabilityState.AVAILABLE)
            return created
        }
        if (created == null) {
            _state.value = AdState(AdAvailabilityState.FAILED)
        }
        return created as? View
    }

    fun destroy() { bannerView = null }

    fun markDisabled() { _state.value = AdState(AdAvailabilityState.DISABLED) }
}

/**
 * Interstitial ads, only at natural transition points. Frequency is configurable
 * in Settings (cooldown = "after N navigation actions"); never shown on app open,
 * e-mail reading, composing, sending, or right after a message arrives (rule #29).
 * If no ad is loaded we continue normally (never block the user).
 */
class InterstitialAdManager(
    private val context: Context,
    private val session: LevelPlaySession = Sessions.current(),
) {
    private val _state = MutableStateFlow(AdState(AdAvailabilityState.NOT_CONFIGURED))
    val state: StateFlow<AdState> = _state.asStateFlow()

    private var cooldownActions = 3
    private var actionsSinceShow = 0
    private var lastShowAt = 0L

    fun configureCooldown(actions: Int) { cooldownActions = actions.coerceAtLeast(1) }

    fun maybeShow(activity: Activity?) {
        if (!AdConfig.isConfigured) return
        actionsSinceShow++
        val now = System.currentTimeMillis()
        if (now - lastShowAt < MIN_INTERVAL_MS) return
        if (actionsSinceShow < cooldownActions) return
        if (!session.isInterstitialReady()) return

        actionsSinceShow = 0
        lastShowAt = now
        session.showInterstitial(activity ?: context, "navigation")
    }

    fun preload() {
        if (!AdConfig.isConfigured) return
        _state.value = AdState(AdAvailabilityState.LOADING)
        session.loadInterstitial(AdConfig.interstitialAdUnitId, listener)
    }

    fun onResume(activity: Activity) {}
    fun onPause() {}
    fun onDestroy() { lastShowAt = 0L }

    fun markDisabled() { _state.value = AdState(AdAvailabilityState.DISABLED) }

    private val listener = object : FullscreenListener {
        override fun onLoaded() { _state.value = AdState(AdAvailabilityState.AVAILABLE) }
        override fun onLoadFailed(error: String?) { _state.value = AdState(AdAvailabilityState.UNAVAILABLE) }
        override fun onDisplayed() { _state.value = AdState(AdAvailabilityState.AVAILABLE) }
        override fun onFailedToDisplay(error: String?) { _state.value = AdState(AdAvailabilityState.UNAVAILABLE) }
        override fun onClosed() {
            _state.value = AdState(AdAvailabilityState.NOT_CONFIGURED)
            preload()
        }
    }

    companion object {
        private const val MIN_INTERVAL_MS = 120_000L // 2 min minimum between interstitials
    }
}

/**
 * Rewarded ads. The reward is granted ONLY when [RewardedListener.onRewarded] is
 * delivered by the real SDK — not on shown/closed/waited-time (rules #31–#33).
 */
class RewardedAdManager(
    private val context: Context,
    private val rewardManager: RewardManager,
    private val session: LevelPlaySession = Sessions.current(),
) {
    private val _state = MutableStateFlow(AdState(AdAvailabilityState.NOT_CONFIGURED))
    val state: StateFlow<AdState> = _state.asStateFlow()

    private var pendingReward: (() -> Unit)? = null

    fun preload() {
        if (!AdConfig.isConfigured) return
        session.loadRewarded(AdConfig.rewardedAdUnitId, listener)
    }

    /**
     * @param onGrant runs only after the SDK's completion callback.
     */
    fun show(activity: Activity, onGrant: () -> Unit) {
        if (!AdConfig.isConfigured) { _state.value = AdState(AdAvailabilityState.NOT_CONFIGURED); return }
        if (!session.isRewardedReady()) { _state.value = AdState(AdAvailabilityState.UNAVAILABLE); return }
        pendingReward = onGrant
        session.showRewarded(activity)
    }

    fun markDisabled() { _state.value = AdState(AdAvailabilityState.DISABLED) }

    private fun grant() {
        pendingReward?.invoke()
        pendingReward = null
    }

    private val listener = object : RewardedListener {
        override fun onRewarded() { grant(); _state.value = AdState(AdAvailabilityState.NOT_CONFIGURED); preload() }
        override fun onLoaded() { _state.value = AdState(AdAvailabilityState.AVAILABLE) }
        override fun onLoadFailed(error: String?) { _state.value = AdState(AdAvailabilityState.UNAVAILABLE) }
        override fun onDisplayed() { _state.value = AdState(AdAvailabilityState.AVAILABLE) }
        override fun onFailedToDisplay(error: String?) { _state.value = AdState(AdAvailabilityState.UNAVAILABLE) }
        override fun onClosed() { _state.value = AdState(AdAvailabilityState.NOT_CONFIGURED); pendingReward = null; preload() }
        override fun onAdClicked() {}
    }
}