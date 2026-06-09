package com.xarlord.numbertap.ui

/**
 * Determines which haptic feedback to apply based on game events.
 * Extracted from MainActivity to enable unit testing of the dispatch logic (#159).
 */
enum class HapticEvent {
    CORRECT_TAP,
    COMBO_TAP,
    MILESTONE_TAP,
    WRONG_TAP,
    GAME_OVER
}

/**
 * Returns the appropriate haptic event for a correct tap based on combo and score.
 *
 * Priority: COMBO_TAP (combo >= 3) > MILESTONE_TAP (score % 10 == 0 && score > 0) > CORRECT_TAP
 */
fun hapticForCorrectTap(combo: Int, score: Int): HapticEvent {
    return when {
        combo >= 3 -> HapticEvent.COMBO_TAP
        score > 0 && score % 10 == 0 -> HapticEvent.MILESTONE_TAP
        else -> HapticEvent.CORRECT_TAP
    }
}

/**
 * Returns the haptic event for a wrong tap.
 */
fun hapticForWrongTap(): HapticEvent = HapticEvent.WRONG_TAP

/**
 * Returns the haptic event for game over.
 */
fun hapticForGameOver(): HapticEvent = HapticEvent.GAME_OVER
