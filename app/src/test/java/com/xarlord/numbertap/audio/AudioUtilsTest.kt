package com.xarlord.numbertap.audio

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for AudioUtils — pure audio generation math with zero Android deps.
 * Covers: sine wave generation, WAV encoding, header parsing, pitch mapping.
 * Issue #122: SoundManager audio generation code coverage.
 */
class AudioUtilsTest {

    // === Sine Wave Generation ===

    @Test
    fun `generateSineWave produces correct sample count`() {
        val samples = AudioUtils.generateSineWave(440.0, 0.5, 44100)
        assertEquals(22050, samples.size)
    }

    @Test
    fun `generateSineWave 1 second at 44100Hz produces 44100 samples`() {
        val samples = AudioUtils.generateSineWave(440.0, 1.0, 44100)
        assertEquals(44100, samples.size)
    }

    @Test
    fun `generateSineWave produces non-zero samples`() {
        val samples = AudioUtils.generateSineWave(1000.0, 0.1, 44100)
        assertTrue("Expected non-zero samples", samples.any { it != 0.toShort() })
    }

    @Test
    fun `generateSineWave amplitude is within 16-bit range`() {
        val samples = AudioUtils.generateSineWave(440.0, 1.0, 44100)
        for (s in samples) {
            assertTrue("Sample $s exceeds Short.MAX_VALUE", s <= Short.MAX_VALUE)
            assertTrue("Sample $s below Short.MIN_VALUE", s >= Short.MIN_VALUE)
        }
    }

    @Test
    fun `generateSineWave peak amplitude is approximately half of MAX_VALUE`() {
        // The envelope factor is 0.5, so peak should be ~16384
        val samples = AudioUtils.generateSineWave(100.0, 0.01, 44100)
        val maxAmp = samples.maxOf { abs(it.toInt()) }
        // Allow 20% tolerance for envelope shaping
        assertTrue("Max amplitude $maxAmp too low", maxAmp > 10000)
        assertTrue("Max amplitude $maxAmp too high", maxAmp < 20000)
    }

    @Test
    fun `generateSineWave applies envelope decay in last 20 percent`() {
        val sampleRate = 44100
        val samples = AudioUtils.generateSineWave(440.0, 1.0, sampleRate)
        // First 80% should have full amplitude
        val peak80 = samples.take((samples.size * 0.8).toInt()).maxOf { abs(it.toInt()) }
        // Last sample should be near zero (envelope ramped down)
        val lastSample = abs(samples.last().toInt())
        assertTrue("Last sample $lastSample should be < peak80 * 0.5", lastSample < peak80 * 0.5)
    }

    @Test
    fun `generateSineWave zero duration throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            AudioUtils.generateSineWave(440.0, 0.0, 44100)
        }
    }

    @Test
    fun `generateSineWave very short duration still works`() {
        // 0.0001s at 44100Hz = 4.4 → 4 samples
        val samples = AudioUtils.generateSineWave(440.0, 0.0001, 44100)
        assertTrue("Should produce at least 1 sample", samples.isNotEmpty())
    }

    @Test
    fun `generateSineWave different frequencies produce different samples`() {
        val low = AudioUtils.generateSineWave(100.0, 0.1, 44100)
        val high = AudioUtils.generateSineWave(2000.0, 0.1, 44100)
        assertFalse("Different frequencies should produce different waveforms", low.contentEquals(high))
    }

    @Test
    fun `generateSineWave same params produce identical output`() {
        val a = AudioUtils.generateSineWave(440.0, 0.1, 44100)
        val b = AudioUtils.generateSineWave(440.0, 0.1, 44100)
        assertTrue("Same params should produce identical output", a.contentEquals(b))
    }

    // === Silence Generation ===

    @Test
    fun `generateSilence produces correct sample count`() {
        val silence = AudioUtils.generateSilence(0.5, 44100)
        assertEquals(22050, silence.size)
    }

    @Test
    fun `generateSilence produces all zeros`() {
        val silence = AudioUtils.generateSilence(0.1, 44100)
        assertTrue("All samples should be zero", silence.all { it == 0.toShort() })
    }

    // === WAV Encoding ===

    @Test
    fun `pcmToWav starts with RIFF header`() {
        val pcm = AudioUtils.generateSineWave(440.0, 0.01, 44100)
        val wav = AudioUtils.pcmToWav(pcm, 44100)
        assertEquals('R'.code.toByte(), wav[0])
        assertEquals('I'.code.toByte(), wav[1])
        assertEquals('F'.code.toByte(), wav[2])
        assertEquals('F'.code.toByte(), wav[3])
    }

    @Test
    fun `pcmToWav has WAVE format marker`() {
        val pcm = AudioUtils.generateSineWave(440.0, 0.01, 44100)
        val wav = AudioUtils.pcmToWav(pcm, 44100)
        assertEquals('W'.code.toByte(), wav[8])
        assertEquals('A'.code.toByte(), wav[9])
        assertEquals('V'.code.toByte(), wav[10])
        assertEquals('E'.code.toByte(), wav[11])
    }

    @Test
    fun `pcmToWav total file size is 44 bytes header plus data`() {
        val pcm = AudioUtils.generateSineWave(440.0, 0.1, 44100)
        val wav = AudioUtils.pcmToWav(pcm, 44100)
        assertEquals(44 + pcm.size * 2, wav.size)
    }

    @Test
    fun `pcmToWav with empty PCM produces header only`() {
        val wav = AudioUtils.pcmToWav(ShortArray(0), 44100)
        assertEquals(44, wav.size)
    }

    @Test
    fun `pcmToWav with silence produces correct file`() {
        val silence = ShortArray(100) // 100 samples of silence
        val wav = AudioUtils.pcmToWav(silence, 22050)
        // Data section should be all zeros
        for (i in 44 until wav.size step 2) {
            assertEquals(0, wav[i].toInt())
            assertEquals(0, wav[i + 1].toInt())
        }
    }

    // === WAV Header Parsing ===

    @Test
    fun `parseWavHeader returns correct info for generated WAV`() {
        val pcm = AudioUtils.generateSineWave(440.0, 0.1, 44100)
        val wav = AudioUtils.pcmToWav(pcm, 44100)
        val info = AudioUtils.parseWavHeader(wav)
        assertNotNull(info)
        info!!.let {
            assertEquals(1, it.audioFormat) // PCM
            assertEquals(1, it.numChannels) // Mono
            assertEquals(44100, it.sampleRate)
            assertEquals(16, it.bitsPerSample)
            assertEquals(pcm.size * 2, it.dataSize)
        }
    }

    @Test
    fun `parseWavHeader returns null for garbage data`() {
        assertNull(AudioUtils.parseWavHeader(ByteArray(50)))
    }

    @Test
    fun `parseWavHeader returns null for too-short data`() {
        assertNull(AudioUtils.parseWavHeader(ByteArray(10)))
    }

    @Test
    fun `parseWavHeader returns null for wrong RIFF marker`() {
        val buf = ByteArray(44)
        "FAKE".toByteArray().copyInto(buf, 0)
        assertNull(AudioUtils.parseWavHeader(buf))
    }

    @Test
    fun `parseWavHeader detects different sample rates`() {
        val pcm = ShortArray(100)
        val wav22050 = AudioUtils.pcmToWav(pcm, 22050)
        val info = AudioUtils.parseWavHeader(wav22050)
        assertEquals(22050, info?.sampleRate)
    }

    // === Pitch Mapping ===

    @Test
    fun `pitchForCombo 1 returns base pitch`() {
        assertEquals(1.0f, AudioUtils.pitchForCombo(1), 0.001f)
    }

    @Test
    fun `pitchForCombo 13 returns octave`() {
        assertEquals(2.0f, AudioUtils.pitchForCombo(13), 0.001f)
    }

    @Test
    fun `pitchForCombo beyond max caps at octave`() {
        assertEquals(2.0f, AudioUtils.pitchForCombo(50), 0.001f)
    }

    @Test
    fun `pitchForCombo zero returns base pitch`() {
        assertEquals(1.0f, AudioUtils.pitchForCombo(0), 0.001f)
    }

    @Test
    fun `pitchForCombo negative returns base pitch`() {
        assertEquals(1.0f, AudioUtils.pitchForCombo(-5), 0.001f)
    }

    @Test
    fun `pitchForCombo increases monotonically for 1 to 13`() {
        for (i in 2..13) {
            assertTrue(
                "Combo $i pitch should exceed combo ${i - 1}",
                AudioUtils.pitchForCombo(i) > AudioUtils.pitchForCombo(i - 1)
            )
        }
    }

    @Test
    fun `pitch step 1 is approximately one semitone`() {
        val expected = Math.pow(2.0, 1.0 / 12.0).toFloat()
        assertEquals(expected, AudioUtils.PITCH_STEPS[1], 0.001f)
    }

    @Test
    fun `pitch step 7 is approximately a perfect fifth`() {
        val expected = Math.pow(2.0, 7.0 / 12.0).toFloat()
        assertEquals(expected, AudioUtils.PITCH_STEPS[7], 0.001f)
    }

    @Test
    fun `PITCH_STEPS has 13 entries`() {
        assertEquals(13, AudioUtils.PITCH_STEPS.size)
    }

    // === Roundtrip: generate → encode → parse ===

    @Test
    fun `roundtrip generate encode parse preserves sample rate`() {
        val pcm = AudioUtils.generateSineWave(880.0, 0.05, 22050)
        val wav = AudioUtils.pcmToWav(pcm, 22050)
        val info = AudioUtils.parseWavHeader(wav)
        assertEquals(22050, info?.sampleRate)
    }

    @Test
    fun `roundtrip generate encode parse preserves data size`() {
        val pcm = AudioUtils.generateSineWave(440.0, 0.2, 44100)
        val wav = AudioUtils.pcmToWav(pcm, 44100)
        val info = AudioUtils.parseWavHeader(wav)
        assertEquals(pcm.size * 2, info?.dataSize)
    }
}
