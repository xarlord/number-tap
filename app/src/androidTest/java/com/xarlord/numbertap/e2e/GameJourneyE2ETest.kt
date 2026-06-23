package com.xarlord.numbertap.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.xarlord.numbertap.data.GameConfig
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.TileState
import com.xarlord.numbertap.game.GameEngine
import com.xarlord.numbertap.game.TapResult
import com.xarlord.numbertap.ui.GameOverScreen
import com.xarlord.numbertap.ui.GameScreen
import com.xarlord.numbertap.ui.MenuScreen
import com.xarlord.numbertap.ui.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * E2E critical-flow tests — simulates the FULL user journey.
 *
 * Tests reflect live scenarios a real player would perform:
 * 1. Menu → Start → Play → Game Over → Play Again
 * 2. Menu → Settings → Change Theme → Back
 * 3. Game engine lifecycle: startGame → onTap (correct/wrong) → tick → game over
 * 4. Pause/Resume/Revive lifecycle
 *
 * These exercise the GameEngine + UI layer together as a user experiences them.
 */
class GameJourneyE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val engine = GameEngine()

    // ── Journey 1: Full Game Lifecycle via GameEngine ─────────────

    @Test
    fun journey_startGame_correctTap_incrementsScore() {
        var state = engine.startNewGame(highScore = 0)
        assertTrue("Game should be playing", state.isPlaying)
        assertEquals("Initial score should be 0", 0, state.score)
        assertEquals("Initial target should be 1", 1, state.targetNumber)

        // Find tile with value == targetNumber and tap it
        val target = state.targetNumber
        var tapped = false
        for (row in state.tiles.indices) {
            for (col in state.tiles[row].indices) {
                if (state.tiles[row][col].currentValue == target) {
                    val (newState, result) = engine.onTap(state, row, col)
                    state = newState
                    assertEquals("Tap result should be Correct", TapResult.Correct::class, result::class)
                    tapped = true
                    break
                }
            }
            if (tapped) break
        }
        assertTrue("Should have found and tapped the target tile", tapped)
        assertEquals("Score should be 1 after correct tap", 1, state.score)
        assertEquals("Target should advance to 2", 2, state.targetNumber)
    }

    @Test
    fun journey_wrongTap_decrementsTime_doesNotIncrementScore() {
        var state = engine.startNewGame(highScore = 0)
        val initialTime = state.timeRemaining

        // Find a tile that is NOT the target and tap it
        val target = state.targetNumber
        var tapped = false
        for (row in state.tiles.indices) {
            for (col in state.tiles[row].indices) {
                if (state.tiles[row][col].currentValue != target) {
                    val (newState, result) = engine.onTap(state, row, col)
                    state = newState
                    assertEquals("Tap result should be Wrong", TapResult.Wrong::class, result::class)
                    tapped = true
                    break
                }
            }
            if (tapped) break
        }
        assertTrue("Should have found a non-target tile", tapped)
        assertEquals("Score should remain 0 after wrong tap", 0, state.score)
        assertTrue("Time should decrease after wrong tap", state.timeRemaining < initialTime)
    }

    @Test
    fun journey_multipleCorrectTaps_accumulateScore() {
        var state = engine.startNewGame(highScore = 0)

        repeat(5) {
            val target = state.targetNumber
            var found = false
            for (row in state.tiles.indices) {
                for (col in state.tiles[row].indices) {
                    if (state.tiles[row][col].currentValue == target) {
                        val (newState, _) = engine.onTap(state, row, col)
                        state = newState
                        found = true
                        break
                    }
                }
                if (found) break
            }
            assertTrue("Should find target tile in round ${it + 1}", found)
        }

        assertEquals("Score should be 5 after 5 correct taps", 5, state.score)
        assertEquals("Target should be 6", 6, state.targetNumber)
    }

    @Test
    fun journey_tick_reducesTime_eventuallyGameOver() {
        var state = engine.startNewGame(highScore = 0)
        val initialTime = state.timeRemaining

        // Tick by the full duration — should trigger game over
        state = engine.tick(state, initialTime + 1.0)

        assertFalse("Game should not be playing after time runs out", state.isPlaying)
        assertTrue("Game over flag should be set", state.isGameOver)
        assertEquals("Time should be 0", 0.0, state.timeRemaining, 0.01)
    }

    @Test
    fun journey_tick_smallDelta_reducesTimeContinuesPlaying() {
        var state = engine.startNewGame(highScore = 0)
        val initialTime = state.timeRemaining

        state = engine.tick(state, 5.0)

        assertTrue("Game should still be playing", state.isPlaying)
        assertFalse("Game over should not be set", state.isGameOver)
        assertEquals("Time should decrease by 5", initialTime - 5.0, state.timeRemaining, 0.01)
    }

    // ── Journey 2: Pause / Resume / Revive ────────────────────────

    @Test
    fun journey_pauseGame_isPausedSet() {
        var state = engine.startNewGame(highScore = 0)
        assertFalse("Game should not start paused", state.isPaused)

        state = engine.pause(state)
        assertTrue("Game should be paused after pause()", state.isPaused)
    }

    @Test
    fun journey_resumeGame_clearsPause() {
        var state = engine.startNewGame(highScore = 0)
        state = engine.pause(state)
        assertTrue(state.isPaused)

        state = engine.resume(state)
        assertFalse("Game should not be paused after resume()", state.isPaused)
    }

    @Test
    fun journey_pausedGame_onTap_returnsInvalid() {
        var state = engine.startNewGame(highScore = 0)
        state = engine.pause(state)

        // Any tap while paused should be Invalid
        val (newState, result) = engine.onTap(state, 0, 0)
        assertEquals("Tap while paused should be Invalid", TapResult.Invalid, result)
        assertEquals("State should be unchanged", state, newState)
    }

    @Test
    fun journey_revive_afterGameOver_resumesPlay() {
        var state = engine.startNewGame(highScore = 100)
        state = engine.tick(state, state.timeRemaining + 1.0) // force game over
        assertTrue(state.isGameOver)

        state = engine.revive(state)
        assertTrue("Game should be playing after revive", state.isPlaying)
        assertFalse("Game over flag should be cleared", state.isGameOver)
        assertEquals(
            "Time should be set to revive bonus",
            GameConfig.REVIVE_BONUS_SECONDS, state.timeRemaining, 0.01
        )
    }

    @Test
    fun journey_reviveEligible_whenScoreAboveThreshold() {
        var state = engine.startNewGame(highScore = 100)
        // REVIVE_ELIGIBILITY_THRESHOLD = 0.9; need score >= 90 to be eligible
        // Tap 90 correct tiles to exceed the 90% threshold
        repeat(90) {
            val target = state.targetNumber
            var found = false
            for (row in state.tiles.indices) {
                for (col in state.tiles[row].indices) {
                    if (state.tiles[row][col].currentValue == target) {
                        val (newState, _) = engine.onTap(state, row, col)
                        state = newState
                        found = true
                        break
                    }
                }
                if (found) break
            }
            assertTrue("Should find target tile in round ${it + 1}", found)
        }
        // Force game over
        state = engine.tick(state, state.timeRemaining + 1.0)
        assertTrue("Should be revive eligible", engine.isReviveEligible(state))
    }

    // ── Journey 3: Menu → Settings → Theme ────────────────────────

    @Test
    fun journey_menuSettingsClick_triggersCallback() {
        var settingsClicked = false
        composeTestRule.setContent {
            MenuScreen(
                highScore = 10,
                currentTheme = GameTheme.DEFAULT,
                onStartClick = {},
                onSettingsClick = { settingsClicked = true }
            )
        }
        composeTestRule.onNodeWithText("Settings", substring = true).performClick()
        assertTrue("Settings click should fire callback", settingsClicked)
    }

    @Test
    fun journey_settingsScreen_showsAllThemes() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT,
                soundEnabled = true,
                musicEnabled = true,
                onThemeChange = {},
                onSoundToggle = {},
                onMusicToggle = {},
                onResetHighScore = {},
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chalkboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrix").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default").assertIsDisplayed()
    }

    // ── Journey 4: Game Over Screen ───────────────────────────────

    @Test
    fun journey_gameOver_playAgain_triggersCallback() {
        var playAgainClicked = false
        composeTestRule.setContent {
            GameOverScreen(
                score = 25,
                highScore = 100,
                isNewHighScore = false,
                isReviveEligible = false,
                currentTheme = GameTheme.DEFAULT,
                onPlayAgain = { playAgainClicked = true },
                onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("Play Again", substring = true, useUnmergedTree = true)
            .performClick()
        assertTrue("Play Again should fire callback", playAgainClicked)
    }

    @Test
    fun journey_gameOver_newHighScore_showsBadge() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 150,
                highScore = 150,
                isNewHighScore = true,
                isReviveEligible = false,
                currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {},
                onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("NEW BEST!", substring = true).assertIsDisplayed()
    }

    // ── Journey 5: High Score on Menu ─────────────────────────────

    @Test
    fun journey_highScore_displayedOnMenu() {
        composeTestRule.setContent {
            MenuScreen(highScore = 42, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        composeTestRule.onNodeWithText("BEST: 42").assertIsDisplayed()
    }
}
