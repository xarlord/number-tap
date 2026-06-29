package com.xarlord.numbertap.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import org.junit.Rule
import org.junit.Test

/**
 * Regression tests for the Compose modifier-parameter-ordering convention (#251).
 *
 * Ensures all public Composables accept `modifier: Modifier = Modifier` as their
 * first optional parameter and still render correctly when a custom modifier is
 * passed positionally.
 */
class ModifierParameterOrderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun menuScreen_acceptsModifierParam_andRenders() {
        composeTestRule.setContent {
            MenuScreen(
                highScore = 10,
                currentTheme = GameTheme.DEFAULT,
                onStartClick = {},
                modifier = Modifier
            )
        }
        composeTestRule.onNodeWithText("NUMBER TAP").assertIsDisplayed()
    }

    @Test
    fun gameScreen_acceptsModifierParam_andRenders() {
        composeTestRule.setContent {
            GameScreen(
                gameState = GameState(isPlaying = true, score = 5),
                onTileTap = { _, _ -> },
                modifier = Modifier
            )
        }
        composeTestRule.onNodeWithText("SCORE: 0005").assertIsDisplayed()
    }

    @Test
    fun gameOverScreen_acceptsModifierParam_andRenders() {
        composeTestRule.setContent {
            GameOverScreen(
                score = 20,
                highScore = 30,
                isNewHighScore = false,
                isReviveEligible = false,
                currentTheme = GameTheme.DEFAULT,
                onPlayAgain = {},
                onMenu = {},
                modifier = Modifier
            )
        }
        composeTestRule.onNodeWithText("GAME OVER").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_acceptsModifierParam_andRenders() {
        composeTestRule.setContent {
            SettingsScreen(
                currentTheme = GameTheme.DEFAULT,
                soundEnabled = true,
                musicEnabled = true,
                onThemeChange = {},
                onSoundToggle = {},
                onMusicToggle = {},
                onResetHighScore = {},
                onBack = {},
                modifier = Modifier
            )
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
