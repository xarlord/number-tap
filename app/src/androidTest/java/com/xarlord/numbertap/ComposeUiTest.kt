package com.xarlord.numbertap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import com.xarlord.numbertap.ui.MenuScreen
import com.xarlord.numbertap.ui.GameScreen
import com.xarlord.numbertap.ui.GameOverScreen
import com.xarlord.numbertap.ui.SettingsScreen
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive Compose UI tests — all screens, all themes, all states.
 * Issue #121: UI layer test coverage.
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
        composeTestRule.onNodeWithText("Settings", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings", substring = true).assertHasClickAction()
    }

    @Test
    fun menuScreen_zeroHighScore_doesNotShowBestBadge() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        // When highScore is 0, the BEST badge is intentionally hidden (#229)
        composeTestRule.onNodeWithText("BEST: 0").assertDoesNotExist()
    }

    // === GameScreen — rendering ===

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

    @Test
    fun gameScreen_showsFormattedScoreWithLeadingZeros() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(score = 3, isPlaying = true), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0003").assertIsDisplayed()
    }

    @Test
    fun gameScreen_showsLowTime() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(timeRemaining = 3.5, isPlaying = true), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("TIME: 3.5s").assertIsDisplayed()
    }

    @Test
    fun gameScreen_showsTargetOne() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(targetNumber = 1, isPlaying = true), onTileTap = { _, _ -> })
        }
        // Target "1" should appear in both grid and hint — use unmerged tree
        composeTestRule.onNodeWithText("1", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun gameScreen_showsScoreInsteadOfHighScore() {
        // GameScreen does not render a "BEST:" label; it shows SCORE (#229)
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, highScore = 50, score = 7), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0007").assertIsDisplayed()
    }

    // === GameScreen — not playing state ===

    @Test
    fun gameScreen_notPlaying_stillRenders() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = false), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0000").assertIsDisplayed()
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
    fun gameOverScreen_showsScore() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 42, highScore = 100, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        // The score animates from 0 to final value; wait for idle (#229)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_showsPlayAgain() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onAllNodesWithText("Play Again", substring = true, useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun gameOverScreen_reviveEligible_showsRevive() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 45, highScore = 50, isNewHighScore = false,
                isReviveEligible = true, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        // Revive button text is "+5 SECONDS  (Watch Ad)" (#229)
        composeTestRule.onNodeWithText("+5 SECONDS", substring = true).assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_notReviveEligible_noRevive() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 5, highScore = 100, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("+5 SECONDS", substring = true).assertDoesNotExist()
    }

    @Test
    fun gameOverScreen_newHighScore_showsIndicator() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 100, highScore = 100, isNewHighScore = true,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("NEW BEST!", substring = true).assertIsDisplayed()
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

    @Test
    fun settingsScreen_showsThemeOptions() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT, soundEnabled = true, musicEnabled = true,
                onThemeChange = {}, onSoundToggle = {}, onMusicToggle = {},
                onResetHighScore = {}, onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chalkboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrix").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_soundOff_stateReflects() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT, soundEnabled = false, musicEnabled = true,
                onThemeChange = {}, onSoundToggle = {}, onMusicToggle = {},
                onResetHighScore = {}, onBack = {}
            )
        }
        // The switch for Sound Effects should be displayed (off state)
        composeTestRule.onNodeWithText("Sound Effects").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsBackButton() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT, soundEnabled = true, musicEnabled = true,
                onThemeChange = {}, onSoundToggle = {}, onMusicToggle = {},
                onResetHighScore = {}, onBack = {}
            )
        }
        // Back button contentDescription is "Go back" (#229)
        composeTestRule.onNodeWithContentDescription("Go back", substring = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsVersionInfo() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT, soundEnabled = true, musicEnabled = true,
                onThemeChange = {}, onSoundToggle = {}, onMusicToggle = {},
                onResetHighScore = {}, onBack = {}
            )
        }
        // Version text is "Number Tap v{version}" (#229)
        composeTestRule.onNodeWithText("Number Tap v", substring = true).assertIsDisplayed()
    }

    // === Theme rendering — all 4 themes ===

    @Test
    fun gameScreen_defaultTheme() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, currentTheme = GameTheme.DEFAULT), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0000").assertIsDisplayed()
    }

    @Test
    fun gameScreen_terminalTheme() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, currentTheme = GameTheme.TERMINAL), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithText("SCORE: 0000").assertIsDisplayed()
    }

    @Test
    fun gameScreen_chalkboardTheme() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, currentTheme = GameTheme.CHALKBOARD), onTileTap = { _, _ -> })
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

    @Test
    fun gameOverScreen_terminalTheme() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.TERMINAL,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("GAME OVER").assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_chalkboardTheme() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.CHALKBOARD,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("GAME OVER").assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_matrixTheme() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.MATRIX,
                onPlayAgain = {}, onMenu = {}
            )
        }
        composeTestRule.onNodeWithText("GAME OVER").assertIsDisplayed()
    }

    // === Theme in menu ===

    @Test
    fun menuScreen_terminalTheme() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.TERMINAL, onStartClick = {})
        }
        composeTestRule.onNodeWithText("NUMBER TAP").assertIsDisplayed()
    }

    @Test
    fun menuScreen_chalkboardTheme() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.CHALKBOARD, onStartClick = {})
        }
        composeTestRule.onNodeWithText("NUMBER TAP").assertIsDisplayed()
    }

    @Test
    fun menuScreen_matrixTheme() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.MATRIX, onStartClick = {})
        }
        composeTestRule.onNodeWithText("NUMBER TAP").assertIsDisplayed()
    }
}
