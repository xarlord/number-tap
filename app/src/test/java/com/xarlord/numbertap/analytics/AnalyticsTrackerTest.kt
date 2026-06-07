package com.xarlord.numbertap.analytics

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AnalyticsTracker.
 * Issue #95: Verify event tracking interface contract.
 */
class AnalyticsTrackerTest {

    @Before
    fun setUp() {
        AnalyticsTracker.setEnabled(true)
    }

    @After
    fun tearDown() {
        AnalyticsTracker.setEnabled(true)
    }

    @Test
    fun `all analytics events have unique names`() {
        val names = AnalyticsEvent.entries.map { it.name }.toSet()
        assertEquals(AnalyticsEvent.entries.size, names.size)
    }

    @Test
    fun `expected events exist`() {
        val expected = setOf(
            "SESSION_START", "SESSION_END", "GAME_START", "GAME_OVER",
            "TAP_CORRECT", "TAP_WRONG", "MILESTONE", "POWERUP_USED",
            "MISSION_COMPLETED", "DAILY_LOGIN",
            "AD_INTERSTITIAL_SHOWN", "AD_REWARDED_SHOWN",
            "AD_REWARDED_EARNED", "AD_REWARDED_FAILED", "REVIVE_USED"
        )
        val actual = AnalyticsEvent.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `track does not throw for simple event`() {
        // Should not throw
        AnalyticsTracker.track(AnalyticsEvent.SESSION_START)
    }

    @Test
    fun `track does not throw with params`() {
        AnalyticsTracker.track(AnalyticsEvent.GAME_START, mapOf("score" to 10, "highScore" to 50))
    }

    @Test
    fun `track does not throw when disabled`() {
        AnalyticsTracker.setEnabled(false)
        // Should not throw or crash
        AnalyticsTracker.track(AnalyticsEvent.GAME_OVER, mapOf("score" to 5))
    }

    @Test
    fun `setEnabled toggles state`() {
        AnalyticsTracker.setEnabled(false)
        // After disabling, track should silently skip — verify no exception
        AnalyticsTracker.track(AnalyticsEvent.TAP_CORRECT)

        AnalyticsTracker.setEnabled(true)
        AnalyticsTracker.track(AnalyticsEvent.TAP_CORRECT)
    }

    @Test
    fun `convenience sessionStart does not throw`() {
        AnalyticsTracker.sessionStart()
    }

    @Test
    fun `convenience sessionEnd does not throw`() {
        AnalyticsTracker.sessionEnd()
    }

    @Test
    fun `convenience gameStart does not throw`() {
        AnalyticsTracker.gameStart(score = 0, highScore = 100)
    }

    @Test
    fun `convenience gameOver does not throw`() {
        AnalyticsTracker.gameOver(score = 42, highScore = 100, timeRemaining = 3.5)
    }

    @Test
    fun `convenience tapCorrect does not throw`() {
        AnalyticsTracker.tapCorrect(score = 10, combo = 5)
    }

    @Test
    fun `convenience tapWrong does not throw`() {
        AnalyticsTracker.tapWrong(score = 10)
    }

    @Test
    fun `convenience milestone does not throw`() {
        AnalyticsTracker.milestone(score = 50)
    }

    @Test
    fun `convenience powerUpUsed does not throw`() {
        AnalyticsTracker.powerUpUsed(type = "HIGHLIGHT")
    }

    @Test
    fun `convenience missionCompleted does not throw`() {
        AnalyticsTracker.missionCompleted(missionId = "daily_2026-01-15_0")
    }

    @Test
    fun `convenience dailyLogin does not throw`() {
        AnalyticsTracker.dailyLogin(streak = 3, coinsAwarded = 20)
    }

    @Test
    fun `empty params does not throw`() {
        AnalyticsTracker.track(AnalyticsEvent.MILESTONE, emptyMap())
    }

    @Test
    fun `large params map does not throw`() {
        val largeParams = (1..50).associate { "key_$it" to it }
        AnalyticsTracker.track(AnalyticsEvent.GAME_OVER, largeParams)
    }
}
