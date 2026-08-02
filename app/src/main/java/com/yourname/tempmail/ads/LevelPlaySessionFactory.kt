package com.yourname.tempmail.ads

import android.content.Context

/**
 * Builds the [LevelPlaySession] used by the app.
 *
 * The real Mediation SDK (com.unity3d.ads-mediation:mediation-sdk) is optional:
 * the app still compiles and runs without it, but ads simply stay disabled. To
 * turn them on: add the dependency and supply real IDs in local.properties, then
 * read the README for the single file where the concrete LevelPlay API is wired.
 */
object LevelPlaySessionFactory {

    fun create(context: Context): LevelPlaySession {
        val session = if (detectSdkOnClasspath()) {
            // Real SDK present — the wrapper below implements the session contract.
            LevelPlaySessionImpl()
        } else {
            NoOpLevelPlaySession
        }
        Sessions.set(session)
        return session
    }

    private fun detectSdkOnClasspath(): Boolean = runCatching {
        // Real LevelPlay/Unity Mediation SDK entry point (9.x).
        Class.forName("com.unity3d.mediation.MediationSdk")
    }.isSuccess
}

/** Placeholder real session; implement accords the requested API. */
class LevelPlaySessionImpl : LevelPlaySession {
    override fun initialize(appKey: String, listener: LevelPlayInitListener) {
        listener.onInitFailure("not_configured")
    }

    override fun createBanner(adUnitId: String, listener: BannerListener): Any? = null
    override fun loadInterstitial(adUnitId: String, listener: FullscreenListener) =
        listener.onLoadFailed("not_configured")
    override fun isInterstitialReady(): Boolean = false
    override fun showInterstitial(context: Context, placement: String) {}
    override fun loadRewarded(adUnitId: String, listener: RewardedListener) =
        listener.onLoadFailed("not_configured")
    override fun isRewardedReady(): Boolean = false
    override fun showRewarded(context: Context) {}
    override fun isInitialized(): Boolean = false
}