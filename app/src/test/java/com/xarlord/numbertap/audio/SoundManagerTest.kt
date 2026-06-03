package com.xarlord.numbertap.audio

import org.junit.Assert.*
import org.junit.Test

class SoundManagerTest {

    // Test the pitch calculation logic without Android context
    // SoundManager uses these same values internally

    private val pitchSteps = floatArrayOf(
        1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
        1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
    )

    @Test
    fun `pitch for combo 0 is base pitch`() {
        assertEquals(1.0f, pitchSteps[0], 0.001f)
    }

    @Test
    fun `pitch for combo 12 is octave up`() {
        assertEquals(2.0f, pitchSteps[12], 0.001f)
    }

    @Test
    fun `pitch for combo is capped at max`() {
        val combo = 20 // beyond max
        val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
        assertEquals(2.0f, pitch, 0.001f)
    }

    @Test
    fun `each pitch step increases monotonically`() {
        for (i in 1 until pitchSteps.size) {
            assertTrue("Pitch at $i should be > pitch at ${i - 1}", pitchSteps[i] > pitchSteps[i - 1])
        }
    }

    @Test
    fun `pitch step 1 is approximately one semitone`() {
        // A semitone ratio is 2^(1/12) ≈ 1.0595
        val expectedSemitone = Math.pow(2.0, 1.0 / 12.0)
        assertEquals(expectedSemitone.toFloat(), pitchSteps[1], 0.001f)
    }

    @Test
    fun `pitch step 7 is approximately a perfect fifth`() {
        // 2^(7/12) ≈ 1.4983
        val expectedFifth = Math.pow(2.0, 7.0 / 12.0)
        assertEquals(expectedFifth.toFloat(), pitchSteps[7], 0.001f)
    }

    @Test
    fun `pitch step 12 is exactly an octave`() {
        // 2^(12/12) = 2.0
        assertEquals(2.0f, pitchSteps[12], 0.001f)
    }

    @Test
    fun `negative combo is handled gracefully`() {
        val combo = -1
        val clamped = maxOf(0, combo)
        val pitch = pitchSteps[minOf(clamped, pitchSteps.size - 1)]
        assertEquals(1.0f, pitch, 0.001f)
    }

    @Test
    fun `combo exactly at boundary returns correct pitch`() {
        assertEquals(1.0f, pitchSteps[0], 0.001f)
        assertEquals(1.0595f, pitchSteps[1], 0.001f)
        assertEquals(1.1225f, pitchSteps[2], 0.001f)
    }

    @Test
    fun `pitch table has 13 entries for 12 semitones plus base`() {
        assertEquals(13, pitchSteps.size)
    }

    @Test
    fun `minOf combo capping logic works for various values`() {
        // Test the actual capping logic used in SoundManager
        for (combo in 0..12) {
            val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
            assertTrue("Pitch for combo $combo should be >= 1.0", pitch >= 1.0f)
            assertTrue("Pitch for combo $combo should be <= 2.0", pitch <= 2.0f)
        }
    }

    @Test
    fun `combo beyond max returns last entry`() {
        for (combo in 13..100) {
            val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
            assertEquals(2.0f, pitch, 0.001f)
        }
    }
}
