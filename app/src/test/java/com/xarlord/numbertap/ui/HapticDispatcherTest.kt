package com.xarlord.numbertap.ui

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for haptic dispatch logic extracted from MainActivity (#159).
 *
 * Tests verify that the correct HapticEvent is selected based on
 * combo count, score, and tap result.
 */
class HapticDispatcherTest {

    // --- Correct tap: no combo, no milestone ---

    @Test
    fun `correct tap with combo 0 and score 1 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 0, score = 1))
    }

    @Test
    fun `correct tap with combo 1 and score 1 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 1, score = 1))
    }

    @Test
    fun `correct tap with combo 2 and score 1 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 2, score = 1))
    }

    // --- Correct tap: combo >= 3 ---

    @Test
    fun `correct tap with combo 3 returns COMBO_TAP`() {
        assertEquals(HapticEvent.COMBO_TAP, hapticForCorrectTap(combo = 3, score = 1))
    }

    @Test
    fun `correct tap with combo 5 returns COMBO_TAP`() {
        assertEquals(HapticEvent.COMBO_TAP, hapticForCorrectTap(combo = 5, score = 1))
    }

    @Test
    fun `correct tap with combo 10 returns COMBO_TAP`() {
        assertEquals(HapticEvent.COMBO_TAP, hapticForCorrectTap(combo = 10, score = 1))
    }

    // --- Correct tap: milestone (score % 10 == 0) ---

    @Test
    fun `correct tap at score 10 with combo 0 returns MILESTONE_TAP`() {
        assertEquals(HapticEvent.MILESTONE_TAP, hapticForCorrectTap(combo = 0, score = 10))
    }

    @Test
    fun `correct tap at score 20 with combo 1 returns MILESTONE_TAP`() {
        assertEquals(HapticEvent.MILESTONE_TAP, hapticForCorrectTap(combo = 1, score = 20))
    }

    @Test
    fun `correct tap at score 50 with combo 2 returns MILESTONE_TAP`() {
        assertEquals(HapticEvent.MILESTONE_TAP, hapticForCorrectTap(combo = 2, score = 50))
    }

    // --- Combo takes priority over milestone ---

    @Test
    fun `combo 3 at milestone score 10 returns COMBO_TAP not MILESTONE_TAP`() {
        assertEquals(HapticEvent.COMBO_TAP, hapticForCorrectTap(combo = 3, score = 10))
    }

    @Test
    fun `combo 5 at milestone score 30 returns COMBO_TAP not MILESTONE_TAP`() {
        assertEquals(HapticEvent.COMBO_TAP, hapticForCorrectTap(combo = 5, score = 30))
    }

    // --- Edge cases ---

    @Test
    fun `score 0 does not trigger milestone even though 0 mod 10 is 0`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 0, score = 0))
    }

    @Test
    fun `score 0 with combo 2 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 2, score = 0))
    }

    @Test
    fun `score 0 with combo 3 returns COMBO_TAP`() {
        assertEquals(HapticEvent.COMBO_TAP, hapticForCorrectTap(combo = 3, score = 0))
    }

    @Test
    fun `negative combo treated as regular tap`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = -1, score = 5))
    }

    // --- Non-multiple of 10 scores ---

    @Test
    fun `score 9 with combo 0 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 0, score = 9))
    }

    @Test
    fun `score 11 with combo 0 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 0, score = 11))
    }

    @Test
    fun `score 99 with combo 2 returns CORRECT_TAP`() {
        assertEquals(HapticEvent.CORRECT_TAP, hapticForCorrectTap(combo = 2, score = 99))
    }

    @Test
    fun `score 100 with combo 2 returns MILESTONE_TAP`() {
        assertEquals(HapticEvent.MILESTONE_TAP, hapticForCorrectTap(combo = 2, score = 100))
    }

    // --- Wrong tap ---

    @Test
    fun `wrong tap returns WRONG_TAP`() {
        assertEquals(HapticEvent.WRONG_TAP, hapticForWrongTap())
    }

    // --- Game over ---

    @Test
    fun `game over returns GAME_OVER`() {
        assertEquals(HapticEvent.GAME_OVER, hapticForGameOver())
    }
}
