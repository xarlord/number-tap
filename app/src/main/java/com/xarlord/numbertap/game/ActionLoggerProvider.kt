package com.xarlord.numbertap.game

import android.util.Log
import com.xarlord.numbertap.data.ActionType
import com.xarlord.numbertap.data.GameAction
import com.xarlord.numbertap.data.GameConfig
import org.json.JSONObject

/**
 * Injectable action logging interface.
 * Default implementation logs to Android logcat.
 * Tests can provide a mock/stub implementation.
 */
interface ActionLoggerProvider {
    fun log(action: GameAction)
    fun logGameStart(score: Int, highScore: Int)
    fun logTap(row: Int, col: Int, value: Int, target: Int, correct: Boolean, score: Int, time: Double)
    fun logGameOver(score: Int, highScore: Int, time: Double)
    fun logGridTransition(score: Int, newSize: Int)
    fun logTutorialStart()
    fun logTutorialComplete(score: Int)
    fun logPause(score: Int, time: Double)
    fun logResume(score: Int, time: Double)
    fun logRevive(score: Int, time: Double)
    fun logScoreMilestone(score: Int, label: String)
    fun logShare(score: Int)
    fun logTierAnnouncement(score: Int, tier: String)
    fun logError(location: String, message: String)
}

/**
 * Production implementation — logs structured JSON to logcat.
 */
class LogcatActionLogger : ActionLoggerProvider {
    companion object {
        private const val TAG = "NumberTap:Action"
    }

    override fun log(action: GameAction) {
        val json = JSONObject().apply {
            put("ts", action.timestamp)
            put("type", action.type.name)
            put("tile", "[${action.tileRow},${action.tileCol}]")
            put("value", action.tileValue)
            put("target", action.targetValue)
            put("score", action.score)
            put("time", action.timeRemaining)
            if (action.extra.isNotEmpty()) put("extra", action.extra)
        }
        Log.d(TAG, json.toString())
    }

    override fun logGameStart(score: Int, highScore: Int) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.GAME_START, score = score, timeRemaining = GameConfig.INITIAL_TIME_SECONDS))
    }

    override fun logTap(row: Int, col: Int, value: Int, target: Int, correct: Boolean, score: Int, time: Double) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = if (correct) ActionType.TAP_CORRECT else ActionType.TAP_WRONG, tileRow = row, tileCol = col, tileValue = value, targetValue = target, score = score, timeRemaining = time))
    }

    override fun logGameOver(score: Int, highScore: Int, time: Double) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.GAME_OVER, score = score, timeRemaining = time))
    }

    override fun logGridTransition(score: Int, newSize: Int) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.GRID_TRANSITION, score = score, extra = "newGrid=$newSize"))
    }

    override fun logTutorialStart() {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.TUTORIAL_START, extra = "firstTime=true"))
    }

    override fun logTutorialComplete(score: Int) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.TUTORIAL_COMPLETE, score = score, extra = "transitionedToGame=true"))
    }

    override fun logPause(score: Int, time: Double) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.PAUSE, score = score, timeRemaining = time))
    }

    override fun logResume(score: Int, time: Double) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.RESUME, score = score, timeRemaining = time))
    }

    override fun logRevive(score: Int, time: Double) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.REVIVE, score = score, timeRemaining = time, extra = "bonusTime=${GameConfig.REVIVE_BONUS_SECONDS}"))
    }

    override fun logScoreMilestone(score: Int, label: String) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.SCORE_MILESTONE, score = score, extra = "label=$label"))
    }

    override fun logShare(score: Int) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.SHARE, score = score))
    }

    override fun logTierAnnouncement(score: Int, tier: String) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.TIER_ANNOUNCEMENT, score = score, extra = "tier=$tier"))
    }

    override fun logError(location: String, message: String) {
        log(GameAction(timestamp = System.currentTimeMillis(), type = ActionType.ERROR, extra = "error_location=$location error_message=$message"))
    }
}
