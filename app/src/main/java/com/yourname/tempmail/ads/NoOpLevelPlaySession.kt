package com.yourname.tempmail.ads

/**
 * No-op implementation so the app can build and run even before the developer
 * inserts the real LevelPlay dependency/keys. Every method is inert — no fake
 * ad is produced and no fake reward is granted (rule #26, #27, #31).
 */
object NoOpLevelPlaySession : LevelPlaySession {
    override fun initialize(appKey: String, listener: LevelPlayInitListener) {
        listener.onInitFailure("ads.not.configured")
    }

    override fun createBanner(adUnitId: String, listener: BannerListener): Any? = null

    override fun loadInterstitial(adUnitId: String, listener: FullscreenListener) {
        listener.onLoadFailed("ads.not.configured")
    }

    override fun isInterstitialReady(): Boolean = false
    override fun showInterstitial(context: android.content.Context, placement: String) {}

    override fun loadRewarded(adUnitId: String, listener: RewardedListener) {
        listener.onLoadFailed("ads.not.configured")
    }

    override fun isRewardedReady(): Boolean = false
    override fun showRewarded(context: android.content.Context) {}
    override fun isInitialized(): Boolean = false
}