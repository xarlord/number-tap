package com.xarlord.numbertap.ui

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MenuScreen animation constants and derived logic (#161).
 *
 * Tests verify constant values used in pulse animation, logo float,
 * and button pulse to prevent accidental regression.
 */
class MenuScreenTest {

    // --- Pulse scale tests ---

    @Test
    fun `pulse scale min is 0 point 97`() {
        assertEquals(0.97f, PULSE_SCALE_MIN, 0.001f)
    }

    @Test
    fun `pulse scale max is 1 point 03`() {
        assertEquals(1.03f, PULSE_SCALE_MAX, 0.001f)
    }

    @Test
    fun `pulse scale min is less than 1`() {
        assertTrue(PULSE_SCALE_MIN < 1f)
    }

    @Test
    fun `pulse scale max is greater than 1`() {
        assertTrue(PULSE_SCALE_MAX > 1f)
    }

    @Test
    fun `pulse scale min is less than max`() {
        assertTrue(PULSE_SCALE_MIN < PULSE_SCALE_MAX)
    }

    @Test
    fun `pulse range is symmetrical around 1`() {
        val belowDelta = 1f - PULSE_SCALE_MIN
        val aboveDelta = PULSE_SCALE_MAX - 1f
        assertEquals(belowDelta, aboveDelta, 0.001f)
    }

    // --- Pulse duration tests ---

    @Test
    fun `pulse duration is 1200ms`() {
        assertEquals(1200, PULSE_DURATION_MS)
    }

    @Test
    fun `pulse duration is positive`() {
        assertTrue(PULSE_DURATION_MS > 0)
    }

    // --- Logo float tests ---

    @Test
    fun `logo float min is minus 4 dp`() {
        assertEquals(-4f, LOGO_FLOAT_MIN_DP, 0.001f)
    }

    @Test
    fun `logo float max is 4 dp`() {
        assertEquals(4f, LOGO_FLOAT_MAX_DP, 0.001f)
    }

    @Test
    fun `logo float min is negative`() {
        assertTrue(LOGO_FLOAT_MIN_DP < 0f)
    }

    @Test
    fun `logo float max is positive`() {
        assertTrue(LOGO_FLOAT_MAX_DP > 0f)
    }

    @Test
    fun `logo float range is symmetrical around 0`() {
        assertEquals(-LOGO_FLOAT_MIN_DP, LOGO_FLOAT_MAX_DP, 0.001f)
    }

    @Test
    fun `logo float duration is 3000ms`() {
        assertEquals(3000, LOGO_FLOAT_DURATION_MS)
    }

    @Test
    fun `logo float duration is positive`() {
        assertTrue(LOGO_FLOAT_DURATION_MS > 0)
    }

    // --- Button pulse tests ---

    @Test
    fun `button pulse scale is 1 point 05`() {
        assertEquals(1.05f, BUTTON_PULSE_SCALE, 0.001f)
    }

    @Test
    fun `button pulse scale is greater than 1`() {
        assertTrue(BUTTON_PULSE_SCALE > 1f)
    }

    @Test
    fun `button pulse damping is 0 point 4`() {
        assertEquals(0.4f, BUTTON_PULSE_DAMPING, 0.001f)
    }

    @Test
    fun `button pulse damping is in valid range`() {
        assertTrue(BUTTON_PULSE_DAMPING in 0f..1f)
    }

    @Test
    fun `button pulse cycle is 800ms`() {
        assertEquals(800L, BUTTON_PULSE_CYCLE_MS)
    }

    @Test
    fun `button pulse cycle is positive`() {
        assertTrue(BUTTON_PULSE_CYCLE_MS > 0)
    }
}
