package com.xarlord.numbertap.game

/**
 * Legacy singleton adapter. Prefer injecting ActionLoggerProvider directly.
 * This will be removed once all callers are migrated.
 */
object ActionLogger : ActionLoggerProvider by LogcatActionLogger()

/**
 * No-op implementation for use in tests. All calls are silently discarded.
 */
class NoOpActionLogger : ActionLoggerProvider {
    override fun log(action: com.xarlord.numbertap.data.GameAction) {}
    override fun logGameStart(score: Int, highScore: Int) {}
    override fun logTap(row: Int, col: Int, value: Int, target: Int, correct: Boolean, score: Int, time: Double) {}
    override fun logGameOver(score: Int, highScore: Int, time: Double) {}
    override fun logGridTransition(score: Int, newSize: Int) {}
    override fun logTutorialStart() {}
    override fun logTutorialComplete(score: Int) {}
    override fun logPause(score: Int, time: Double) {}
    override fun logResume(score: Int, time: Double) {}
    override fun logRevive(score: Int, time: Double) {}
    override fun logScoreMilestone(score: Int, label: String) {}
    override fun logShare(score: Int) {}
    override fun logTierAnnouncement(score: Int, tier: String) {}
    override fun logError(location: String, message: String) {}
}
