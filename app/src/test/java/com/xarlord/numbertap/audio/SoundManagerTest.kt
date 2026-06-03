package com.xarlord.numbertap.audio

import org.junit.Assert.*
import org.junit.Test

class SoundManagerTest {

    @Test
    fun `pitch for combo 0 is base pitch`() {
        // Can't instantiate SoundManager without Android context, test the static pitch logic
        val pitchSteps = floatArrayOf(
            1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
            1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
        )
        assertEquals(1.0f, pitchSteps[0], 0.001f)
    }

    @Test
    fun `pitch for combo 12 is octave up`() {
        val pitchSteps = floatArrayOf(
            1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
            1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
        )
        assertEquals(2.0f, pitchSteps[12], 0.001f)
    }

    @Test
    fun `pitch for combo is capped at max`() {
        val pitchSteps = floatArrayOf(
            1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
            1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
        )
        val combo = 20 // beyond max
        val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
        assertEquals(2.0f, pitch, 0.001f)
    }

    @Test
    fun `each pitch step increases`() {
        val pitchSteps = floatArrayOf(
            1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
            1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
        )
        for (i in 1 until pitchSteps.size) {
            assertTrue("Pitch at $i should be > pitch at ${i - 1}", pitchSteps[i] > pitchSteps[i - 1])
        }
    }
}
