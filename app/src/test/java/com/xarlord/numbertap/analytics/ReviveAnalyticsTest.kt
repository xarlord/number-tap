package com.xarlord.numbertap.analytics

import com.xarlord.numbertap.data.GameState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviveAnalyticsTest {

    private val gameOver = GameState(
        isPlaying = false,
        isGameOver = true,
        score = 10,
        highScore = 20
    )

    @Test
    fun `accepted rewarded revive emits once with source`() {
        val events = mutableListOf<Pair<AnalyticsEvent, Map<String, Any>>>()
        val revived = gameOver.copy(isPlaying = true, isGameOver = false, timeRemaining = 5.0)

        val applied = trackReviveIfApplied(gameOver, revived, "rewarded_ad") { event, params ->
            events += event to params
        }

        assertTrue(applied)
        assertTrue(events == listOf(AnalyticsEvent.REVIVE_USED to mapOf("revive_source" to "rewarded_ad")))
    }

    @Test
    fun `accepted fallback revive uses distinct source`() {
        val events = mutableListOf<Pair<AnalyticsEvent, Map<String, Any>>>()
        val revived = gameOver.copy(isPlaying = true, isGameOver = false, timeRemaining = 5.0)

        trackReviveIfApplied(gameOver, revived, "fallback") { event, params ->
            events += event to params
        }

        assertTrue(events == listOf(AnalyticsEvent.REVIVE_USED to mapOf("revive_source" to "fallback")))
    }

    @Test
    fun `rejected or duplicate coin revive emits nothing`() {
        val events = mutableListOf<Pair<AnalyticsEvent, Map<String, Any>>>()
        val alreadyPlaying = gameOver.copy(isPlaying = true, isGameOver = false, timeRemaining = 5.0)

        val staleApplied = trackReviveIfApplied(alreadyPlaying, alreadyPlaying, "coins") { event, params ->
            events += event to params
        }
        val unchangedApplied = trackReviveIfApplied(gameOver, gameOver, "coins") { event, params ->
            events += event to params
        }

        assertFalse(staleApplied)
        assertFalse(unchangedApplied)
        assertTrue(events.isEmpty())
    }
}
