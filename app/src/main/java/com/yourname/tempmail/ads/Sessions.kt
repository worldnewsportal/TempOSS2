package com.yourname.tempmail.ads

/**
 * Process-wide holder for the active [LevelPlaySession]. Defaults to the no-op
 * implementation; [LevelPlaySessionFactory.create] swaps in the real SDK wrapper
 * when the dependency & credentials are present.
 */
object Sessions {
    @Volatile
    private var currentInstance: LevelPlaySession = NoOpLevelPlaySession

    fun current(): LevelPlaySession = currentInstance

    fun set(session: LevelPlaySession) { currentInstance = session }
}