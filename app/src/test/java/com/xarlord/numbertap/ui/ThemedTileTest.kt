package com.xarlord.numbertap.ui

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ThemedTile animation constants and derived logic (#162).
 *
 * Tests verify constant values used in scale bounce, fade animation,
 * and entry animation to prevent accidental regression.
 */
class ThemedTileTest {

    // --- Bounce scale tests ---

    @Test
    fun `bounce scale is 0 point 85`() {
        assertEquals(0.85f, BOUNCE_SCALE, 0.001f)
    }

    @Test
    fun `bounce scale is less than 1`() {
        assertTrue(BOUNCE_SCALE < 1f)
    }

    @Test
    fun `bounce scale is greater than 0`() {
        assertTrue(BOUNCE_SCALE > 0f)
    }

    // --- Fade frame delay tests ---

    @Test
    fun `fade frame delay is 120ms`() {
        assertEquals(120L, FADE_FRAME_DELAY_MS)
    }

    @Test
    fun `fade frame delay is positive`() {
        assertTrue(FADE_FRAME_DELAY_MS > 0)
    }

    // --- Entry scale tests ---

    @Test
    fun `entry scale initial is 0 point 6`() {
        assertEquals(0.6f, ENTRY_SCALE_INITIAL, 0.001f)
    }

    @Test
    fun `entry scale initial is less than 1`() {
        assertTrue(ENTRY_SCALE_INITIAL < 1f)
    }

    @Test
    fun `entry scale initial is greater than 0`() {
        assertTrue(ENTRY_SCALE_INITIAL > 0f)
    }

    // --- Target glow alpha tests ---

    @Test
    fun `target glow alpha is 0 point 4`() {
        assertEquals(0.4f, TARGET_GLOW_ALPHA, 0.001f)
    }

    @Test
    fun `target glow alpha is in valid range`() {
        assertTrue(TARGET_GLOW_ALPHA in 0f..1f)
    }

    // --- Target hint alpha tests ---

    @Test
    fun `target hint alpha is 0 point 3`() {
        assertEquals(0.3f, TARGET_HINT_ALPHA, 0.001f)
    }

    @Test
    fun `target hint alpha is in valid range`() {
        assertTrue(TARGET_HINT_ALPHA in 0f..1f)
    }

    // --- Target glow background alpha tests ---

    @Test
    fun `target glow background alpha is 0 point 12`() {
        assertEquals(0.12f, TARGET_GLOW_BG_ALPHA, 0.001f)
    }

    @Test
    fun `target glow background alpha is in valid range`() {
        assertTrue(TARGET_GLOW_BG_ALPHA in 0f..1f)
    }

    @Test
    fun `target glow background alpha is less than hint alpha`() {
        assertTrue(TARGET_GLOW_BG_ALPHA < TARGET_HINT_ALPHA)
    }

    // --- Combined entry + interaction scale invariant tests ---

    @Test
    fun `combined scale at entry start and bounce down does not go negative`() {
        val combinedScale = ENTRY_SCALE_INITIAL * BOUNCE_SCALE
        assertTrue(combinedScale > 0f)
    }

    @Test
    fun `combined scale at entry start and bounce down is small but visible`() {
        val combinedScale = ENTRY_SCALE_INITIAL * BOUNCE_SCALE
        assertTrue(combinedScale > 0.4f)
    }
}
