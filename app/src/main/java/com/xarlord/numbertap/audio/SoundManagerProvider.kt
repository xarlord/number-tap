package com.xarlord.numbertap.audio

/**
 * Injectable sound effects interface.
 * Default implementation uses Android SoundPool + AudioTrack.
 * Tests can provide a no-op or mock implementation.
 */
interface SoundManagerProvider {
    fun playSuccess(combo: Int)
    fun playFailure()
    fun playCountdownTick()
    fun playGameOver()
    fun playMilestone()
    fun playComboBreak()
    fun startBGMusic()
    fun stopBGMusic()
    fun release()
}

/**
 * No-op implementation for testing or when sound is disabled.
 */
class NoOpSoundManager : SoundManagerProvider {
    var released = false; private set
    override fun playSuccess(combo: Int) {}
    override fun playFailure() {}
    override fun playCountdownTick() {}
    override fun playGameOver() {}
    override fun playMilestone() {}
    override fun playComboBreak() {}
    override fun startBGMusic() {}
    override fun stopBGMusic() {}
    override fun release() { released = true }
}
