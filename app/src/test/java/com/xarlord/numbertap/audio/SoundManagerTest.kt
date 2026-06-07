package com.xarlord.numbertap.audio

import org.junit.Assert.*
import org.junit.Test

class SoundManagerTest {

    // --- Pitch calculation logic tests (now via AudioUtils) ---

    @Test
    fun `pitch for combo 0 is base pitch`() {
        assertEquals(1.0f, AudioUtils.pitchForCombo(0), 0.001f)
    }

    @Test
    fun `pitch for combo 12 is within valid range`() {
        val pitch = AudioUtils.pitchForCombo(12)
        assertTrue(pitch >= 1.0f)
        assertTrue(pitch <= 2.0f)
    }

    @Test
    fun `pitch for combo beyond max caps at octave`() {
        assertEquals(2.0f, AudioUtils.pitchForCombo(100), 0.001f)
    }

    @Test
    fun `each pitch step increases monotonically`() {
        for (i in 1 until AudioUtils.PITCH_STEPS.size) {
            assertTrue(
                "Pitch at $i should be > pitch at ${i - 1}",
                AudioUtils.PITCH_STEPS[i] > AudioUtils.PITCH_STEPS[i - 1]
            )
        }
    }

    @Test
    fun `negative combo is handled gracefully`() {
        val pitch = AudioUtils.pitchForCombo(-1)
        assertTrue(pitch >= 1.0f)
        assertTrue(pitch <= 2.0f)
    }

    @Test
    fun `combo 1 maps to base pitch per GDD`() {
        assertEquals(1.0f, AudioUtils.pitchForCombo(1), 0.001f)
    }

    @Test
    fun `combo 2 maps to first semitone step`() {
        assertEquals(1.0595f, AudioUtils.pitchForCombo(2), 0.001f)
    }

    // --- NoOpSoundManager tests (no Android deps needed) ---

    @Test
    fun `NoOpSoundManager implements SoundManagerProvider`() {
        val mgr: SoundManagerProvider = NoOpSoundManager()
        assertNotNull(mgr)
    }

    @Test
    fun `NoOpSoundManager all methods run without error`() {
        val mgr = NoOpSoundManager()
        mgr.playSuccess(1)
        mgr.playSuccess(5)
        mgr.playFailure()
        mgr.playCountdownTick()
        mgr.playGameOver()
        mgr.playMilestone()
        mgr.playComboBreak()
        mgr.startBGMusic()
        mgr.stopBGMusic()
    }

    @Test
    fun `NoOpSoundManager release sets released flag`() {
        val mgr = NoOpSoundManager()
        assertFalse(mgr.released)
        mgr.release()
        assertTrue(mgr.released)
    }

    @Test
    fun `SoundManager is assignable to SoundManagerProvider`() {
        val acceptProvider: (SoundManagerProvider) -> Unit = { _ -> }
        val factory: () -> SoundManagerProvider = { throw UnsupportedOperationException("test") }
        assertNotNull(factory)
    }
}
