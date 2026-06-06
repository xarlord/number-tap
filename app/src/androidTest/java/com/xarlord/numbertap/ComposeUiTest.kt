package com.xarlord.numbertap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.ui.MenuScreen
import com.xarlord.numbertap.ui.GameScreen
import com.xarlord.numbertap.ui.GameOverScreen
import com.xarlord.numbertap.ui.SettingsScreen
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests — rendering verification for all screens and themes.
 * Click tests are limited on API 35 emulator due to infinite animation interference.
 */
class ComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // === MenuScreen ===

    @Test
    fun menuScreen_showsTitle() {
        composeTestRule.setContent {
            MenuScreen(highScore = 42, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        composeTestRule.onNodeWithText("NUMBER TAP").assertIsDisplayed()
    }

    @Test
    fun menuScreen_showsHighScore() {
        composeTestRule.setContent {
            MenuScreen(highScore = 99, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        composeTestRule.onNodeWithText("BEST: 99").assertIsDisplayed()
    }

    @Test
    fun menuScreen_showsStartButton() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        composeTestRule.onNodeWithText("START").assertIsDisplayed()
        composeTestRule.onNodeWithText("START").assertHasClickAction()
    }

    @Test
    fun menuScreen_showsAllThemes() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chalkboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrix").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default").assertIsDisplayed()
    }

    @Test
    fun menuScreen_settingsButtonExists() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {}, onSettingsClick = {})
        }
        // Settings button has ⚙ prefix — use substring match
        composeTestRule.onNodeWithText("Settings", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings", substring = true).assertHasClickAction()
    }

    // === GameScreen ===

    @Test
    fun gameScreen_showsScore() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(score = 7, isPlaying = true), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0007").assertIsDisplayed()
    }

    @Test
    fun gameScreen_showsTime() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(timeRemaining = 25.0, isPlaying = true), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("TIME: 25.0s").assertIsDisplayed()
    }

    @Test
    fun gameScreen_showsTarget() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(targetNumber = 5, isPlaying = true), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("5", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun gameScreen_showsBottomPanel() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, totalTaps = 10), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("TAPS").assertIsDisplayed()
    }

    @Test
    fun gameScreen_pauseButtonExists() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true), onTileTap = { _, _ -> }, onPauseClick = {})
        }
        composeTestRule.onNodeWithContentDescription("Pause").assertIsDisplayed()
    }

    // === GameOverScreen ===

    @Test
    fun gameOverScreen_showsGameOver() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 42, highScore = 100, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("GAME OVER").assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_showsButtons() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        // Use case-insensitive search via assertExists
        composeTestRule.onAllNodesWithText("Play Again", substring = true, useUnmergedTree = true)
            .assertCountEquals(1)
    }

    // === SettingsScreen ===

    @Test
    fun settingsScreen_showsTitle() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT, soundEnabled = true, musicEnabled = true,
                onThemeChange = {}, onSoundToggle = {}, onMusicToggle = {},
                onResetHighScore = {}, onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsToggles() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT, soundEnabled = true, musicEnabled = true,
                onThemeChange = {}, onSoundToggle = {}, onMusicToggle = {},
                onResetHighScore = {}, onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Sound Effects").assertIsDisplayed()
        composeTestRule.onNodeWithText("Background Music").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset High Score").assertIsDisplayed()
    }

    // === Theme rendering ===

    @Test
    fun gameScreen_terminalTheme() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, currentTheme = GameTheme.TERMINAL), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0000").assertIsDisplayed()
    }

    @Test
    fun gameScreen_matrixTheme() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, currentTheme = GameTheme.MATRIX), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0000").assertIsDisplayed()
    }
}
