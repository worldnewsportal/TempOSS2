package com.yourname.tempmail

import android.app.Application
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Application entry point. Owns the [AppContainer] dependency graph and a small
 * scope for one-off startup jobs (ads init, background sync scheduling).
 */
class TempmailApp : Application() {

    val container by lazy { AppContainer(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container.ads.initialize()
        appScope.launch {
            SyncWorker.schedule(this@TempmailApp)
        }
    }

    override fun onTerminate() {
        container.providers.cancel()
        container.ads.rewardManager.cancel()
        appScope.cancel()
        super.onTerminate()
    }
}