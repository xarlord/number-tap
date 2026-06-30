package com.xarlord.numbertap.ui

import com.xarlord.numbertap.data.GameConfig
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for GameScreen constants and configuration.
 * Note: Composable UI functions require instrumented tests.
 * This file verifies configuration constants used in GameScreen.
 */
class GameScreenTest {

    @Test
    fun `GameConfig INITIAL_TIME_SECONDS is 30`() {
        // Verify the constant used in TimerBar (#263 fix)
        assertEquals(30.0, GameConfig.INITIAL_TIME_SECONDS, 0.01)
    }

    @Test
    fun `GameConfig constants are positive`() {
        // Verify game configuration is valid
        assertTrue(GameConfig.INITIAL_TIME_SECONDS > 0)
    }

    companion object {
        private fun assertTrue(condition: Boolean) {
            if (!condition) throw AssertionError("Condition was false")
        }
    }
}
