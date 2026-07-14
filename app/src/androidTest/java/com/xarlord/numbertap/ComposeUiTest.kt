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
import org.junit.Assert.assertTrue
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
        composeTestRule.onNodeWithText("Terminal").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Chalkboard").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrix").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("✓ Default").performScrollTo().assertIsDisplayed()
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
    fun menuScreen_zeroHighScore_showsBestZero() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        composeTestRule.onNodeWithText("BEST: 0").assertIsDisplayed()
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
    fun tutorialInstruction_isAboveTargetHint_withoutOverlap() {
        composeTestRule.setContent {
            GameScreen(
                gameState = GameState(targetNumber = 1, isPlaying = true, isTutorial = true),
                onTileTap = { _, _ -> }
            )
        }

        val instructionBounds = composeTestRule
            .onNodeWithText("Tap the numbers in order!")
            .fetchSemanticsNode()
            .boundsInRoot
        val targetBounds = composeTestRule
            .onNodeWithContentDescription("Find number 1")
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "Tutorial instruction must finish above the target hint",
            instructionBounds.bottom <= targetBounds.top
        )
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
    fun gameScreen_acceptsStoredHighScoreState() {
        composeTestRule.setContent {
            GameScreen(gameState = GameState(isPlaying = true, highScore = 50), onTileTap = { _, _ -> })
        }
        composeTestRule.onNodeWithContentDescription("Pause").assertIsDisplayed()
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
        composeTestRule.waitUntil(timeoutMillis = 2_000) {
            composeTestRule.onAllNodesWithText("42").fetchSemanticsNodes().isNotEmpty()
        }
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
        composeTestRule
            .onNodeWithContentDescription("Watch ad to revive with 5 extra seconds")
            .assertIsDisplayed()
            .assertHasClickAction()
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
        composeTestRule.onNodeWithText("+5 SECONDS").assertDoesNotExist()
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
        composeTestRule.onNodeWithContentDescription("Go back").assertIsDisplayed().assertHasClickAction()
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
        composeTestRule.onNodeWithText("Number Tap v", substring = true).performScrollTo().assertIsDisplayed()
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

    // === Issue #212: duplicate stats icons (single emoji now) ===

    @Test
    fun menuScreen_coinsDisplay_singleEmoji() {
        composeTestRule.setContent {
            MenuScreen(highScore = 5, currentTheme = GameTheme.DEFAULT, coins = 27, onStartClick = {})
        }
        // coins_display renders "🪙 27" as a single node — no separate duplicate emoji
        composeTestRule.onNodeWithText("🪙 27").assertIsDisplayed()
    }

    @Test
    fun menuScreen_streakDisplay_singleEmoji() {
        composeTestRule.setContent {
            MenuScreen(highScore = 5, currentTheme = GameTheme.DEFAULT, streak = 3, onStartClick = {})
        }
        composeTestRule.onNodeWithText("🔥 3").assertIsDisplayed()
    }

    // === Issue #213: symmetric difficulty toggle ===

    @Test
    fun menuScreen_hardModeToggle_showsBothOptions() {
        var hardMode = false
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, isHardMode = hardMode,
                onHardModeToggle = { hardMode = it }, onStartClick = {})
        }
        // Both NORMAL and HARD options should always be visible and clickable
        composeTestRule.onNodeWithText("NORMAL").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("HARD").assertIsDisplayed().assertHasClickAction()
    }

    // === Issue #214: theme selector shows checkmark on selected ===

    @Test
    fun menuScreen_selectedTheme_showsCheckmark() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        // #214: the selected theme gets a ✓ prefix
        composeTestRule.onNodeWithText("✓ Default").assertIsDisplayed()
    }

    @Test
    fun menuScreen_unselectedTheme_noCheckmark() {
        composeTestRule.setContent {
            MenuScreen(highScore = 0, currentTheme = GameTheme.DEFAULT, onStartClick = {})
        }
        // Non-selected themes should NOT have the checkmark prefix
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
        composeTestRule.onNodeWithText("✓ Terminal").assertDoesNotExist()
    }

    // === Issue #215: GameOverScreen uses localized coin strings ===

    @Test
    fun gameOverScreen_showsLocalizedCoinBalance() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                coinBalance = 42, onPlayAgain = {}, onMenu = {}
            )
        }
        // #215: coins_display "🪙 42" from string resource, not hardcoded duplicate
        composeTestRule.onNodeWithText("🪙 42").assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_showsLocalizedSpendCoinsButton() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 10, highScore = 50, isNewHighScore = false,
                isReviveEligible = false, currentTheme = GameTheme.DEFAULT,
                coinBalance = 100, onPlayAgain = {}, onMenu = {}
            )
        }
        // #215: spend_coins_revive "Spend N Coins (+Ns)" from string resource
        composeTestRule.onNodeWithText("Spend 50 Coins (+5s)").assertIsDisplayed()
    }
}
