package com.xarlord.numbertap.analytics

import android.util.Log
import org.json.JSONObject

/**
 * Simple analytics event tracker — logcat implementation.
 * Issue #95: Structured event tracking without Firebase dependency.
 *
 * Thread-safe singleton. All events are logged as structured JSON to logcat
 * under the tag "NT-Analytics".
 */
object AnalyticsTracker {

    private const val TAG = "NT-Analytics"

    @Volatile
    private var enabled: Boolean = true

    /** Enable or disable tracking (e.g., based on user preferences). */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /** Track a simple event with no parameters. */
    fun track(event: AnalyticsEvent) {
        track(event, emptyMap())
    }

    /** Track an event with additional parameters. */
    fun track(event: AnalyticsEvent, params: Map<String, Any>) {
        if (!enabled) return
        val json = JSONObject().apply {
            put("event", event.name)
            put("ts", System.currentTimeMillis())
            if (params.isNotEmpty()) {
                val paramsJson = JSONObject()
                params.forEach { (k, v) -> paramsJson.put(k, v) }
                put("params", paramsJson)
            }
        }
        Log.d(TAG, json.toString())
    }

    /** Convenience: track session start. */
    fun sessionStart() = track(AnalyticsEvent.SESSION_START)

    /** Convenience: track session end. */
    fun sessionEnd() = track(AnalyticsEvent.SESSION_END)

    /** Convenience: track game start. */
    fun gameStart(score: Int, highScore: Int) =
        track(AnalyticsEvent.GAME_START, mapOf("score" to score, "highScore" to highScore))

    /** Convenience: track game over. */
    fun gameOver(score: Int, highScore: Int, timeRemaining: Double) =
        track(AnalyticsEvent.GAME_OVER, mapOf("score" to score, "highScore" to highScore, "timeRemaining" to timeRemaining))

    /** Convenience: track correct tap. */
    fun tapCorrect(score: Int, combo: Int) =
        track(AnalyticsEvent.TAP_CORRECT, mapOf("score" to score, "combo" to combo))

    /** Convenience: track wrong tap. */
    fun tapWrong(score: Int) =
        track(AnalyticsEvent.TAP_WRONG, mapOf("score" to score))

    /** Convenience: track milestone. */
    fun milestone(score: Int) =
        track(AnalyticsEvent.MILESTONE, mapOf("score" to score))

    /** Convenience: track power-up used. */
    fun powerUpUsed(type: String) =
        track(AnalyticsEvent.POWERUP_USED, mapOf("type" to type))

    /** Convenience: track mission completed. */
    fun missionCompleted(missionId: String) =
        track(AnalyticsEvent.MISSION_COMPLETED, mapOf("missionId" to missionId))

    /** Convenience: track daily login. */
    fun dailyLogin(streak: Int, coinsAwarded: Int) =
        track(AnalyticsEvent.DAILY_LOGIN, mapOf("streak" to streak, "coinsAwarded" to coinsAwarded))
}

/**
 * Analytics events enum. Each value corresponds to a trackable action.
 */
enum class AnalyticsEvent {
    SESSION_START,
    SESSION_END,
    GAME_START,
    GAME_OVER,
    TAP_CORRECT,
    TAP_WRONG,
    MILESTONE,
    POWERUP_USED,
    MISSION_COMPLETED,
    DAILY_LOGIN
}
