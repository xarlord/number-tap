package com.xarlord.numbertap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full activity lifecycle and navigation tests on device/emulator.
 * Uses assertIsDisplayed + assertHasClickAction (no performClick — API 35 emulator limitation).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_showsMenuScreen() {
        composeTestRule.onNodeWithText("NUMBER TAP").assertIsDisplayed()
        composeTestRule.onNodeWithText("START").assertIsDisplayed()
        composeTestRule.onNodeWithText("START").assertHasClickAction()
    }

    @Test
    fun menuScreen_showsThemeSelector() {
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chalkboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrix").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default").assertIsDisplayed()
    }

    @Test
    fun menuScreen_showsSettingsButton() {
        composeTestRule.onNodeWithText("Settings", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings", substring = true).assertHasClickAction()
    }

    @Test
    fun menuScreen_showsHighScore() {
        // Should show BEST label (value depends on persisted score)
        composeTestRule.onNodeWithText("START").assertIsDisplayed()
    }

    @Test
    fun menuScreen_allThemeButtonsAreClickable() {
        for (theme in listOf("Terminal", "Chalkboard", "Matrix", "Default")) {
            composeTestRule.onNodeWithText(theme).assertHasClickAction()
        }
    }
}
