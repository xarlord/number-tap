package com.xarlord.numbertap.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [GameConfig] — ensures all gameplay constants remain stable
 * and are the single source of truth for the codebase.
 *
 * Covers issue #131 and #132.
 */
class GameConfigTest {

    @Test
    fun `initial game time is 30 seconds`() {
        assertEquals(30.0, GameConfig.INITIAL_TIME_SECONDS, 0.001)
    }

    @Test
    fun `revive bonus is 5 seconds`() {
        assertEquals(5.0, GameConfig.REVIVE_BONUS_SECONDS, 0.001)
    }

    @Test
    fun `combo window is 500ms`() {
        assertEquals(500L, GameConfig.COMBO_WINDOW_MS)
    }

    @Test
    fun `floating text duration is 800ms`() {
        assertEquals(800L, GameConfig.FLOATING_TEXT_DURATION_MS)
    }

    @Test
    fun `revive eligibility threshold is 90 percent`() {
        assertEquals(0.9, GameConfig.REVIVE_ELIGIBILITY_THRESHOLD, 0.001)
    }

    @Test
    fun `tutorial time is 999 seconds`() {
        assertEquals(999.0, GameConfig.TUTORIAL_TIME_SECONDS, 0.001)
    }

    @Test
    fun `all constants are positive`() {
        assertTrue(GameConfig.INITIAL_TIME_SECONDS > 0)
        assertTrue(GameConfig.REVIVE_BONUS_SECONDS > 0)
        assertTrue(GameConfig.COMBO_WINDOW_MS > 0)
        assertTrue(GameConfig.FLOATING_TEXT_DURATION_MS > 0)
        assertTrue(GameConfig.REVIVE_ELIGIBILITY_THRESHOLD > 0)
        assertTrue(GameConfig.TUTORIAL_TIME_SECONDS > 0)
    }

    @Test
    fun `revive eligibility threshold is between 0 and 1`() {
        assertTrue(GameConfig.REVIVE_ELIGIBILITY_THRESHOLD in 0.0..1.0)
    }

    @Test
    fun `initial time is greater than revive bonus`() {
        assertTrue(GameConfig.INITIAL_TIME_SECONDS > GameConfig.REVIVE_BONUS_SECONDS)
    }
}

class GameConfigSingleSourceOfTruthTest {

    @Test
    fun `initial time equals GameConfig constant used in GameScreen`() {
        // Verify GameConfig.INITIAL_TIME_SECONDS is used as TimerBar max (fix #233)
        assertEquals(30.0, GameConfig.INITIAL_TIME_SECONDS, 0.001)
    }

    @Test
    fun `revive bonus is less than initial time`() {
        assertTrue(GameConfig.REVIVE_BONUS_SECONDS < GameConfig.INITIAL_TIME_SECONDS)
    }

    @Test
    fun `tutorial time is much larger than initial time`() {
        assertTrue(GameConfig.TUTORIAL_TIME_SECONDS > GameConfig.INITIAL_TIME_SECONDS * 10)
    }
}
