package com.yourname.tempmail.ads

import com.yourname.tempmail.data.db.AdRewardEntity
import com.yourname.tempmail.data.daos.AdRewardDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Persists which rewarded-ad products the user owns. Rewards are granted only
 * via the real SDK completion callback ([RewardedAdManager]); nothing is granted
 * on shown/closed/clicked or after a fixed time period (rules #31–#33).
 */
class RewardManager(
    private val adRewardDao: AdRewardDao,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val owned: StateFlow<List<RewardId>> =
        adRewardDao.observeAll()
            .map { list -> list.mapNotNull { e -> RewardId.entries.firstOrNull { it.id == e.id } } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun owns(rewardId: RewardId): Boolean = adRewardDao.byId(rewardId.id) != null

    suspend fun grant(rewardId: RewardId) {
        adRewardDao.insert(AdRewardEntity(id = rewardId.id, grantedAt = System.currentTimeMillis()))
    }

    /** Cancel the internal scope. Call from Application.onTerminate(). */
    fun cancel() {
        scope.cancel()
    }
}