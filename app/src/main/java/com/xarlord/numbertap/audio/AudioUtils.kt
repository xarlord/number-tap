package com.xarlord.numbertap.audio

import java.io.ByteArrayOutputStream

/**
 * Pure audio utility functions — no Android dependencies.
 * Extracted from SoundManager for unit testability (issue #122).
 */
object AudioUtils {

    /**
     * Generate a sine wave PCM sample with amplitude envelope.
     * @param freqHz Frequency in Hz
     * @param durationSec Duration in seconds
     * @param sampleRate Sample rate (e.g. 44100)
     * @return PCM 16-bit signed samples
     */
    fun generateSineWave(freqHz: Double, durationSec: Double, sampleRate: Int): ShortArray {
        val n = (sampleRate * durationSec).toInt()
        require(n > 0) { "Duration must produce at least 1 sample" }
        val samples = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            val env = if (i > n * 0.8) (n - i).toDouble() / (n * 0.2) else 1.0
            samples[i] = (Math.sin(2.0 * Math.PI * freqHz * t) * Short.MAX_VALUE * 0.5 * env).toInt().toShort()
        }
        return samples
    }

    /**
     * Generate a silence (zero-amplitude) PCM sample.
     */
    fun generateSilence(durationSec: Double, sampleRate: Int): ShortArray {
        val n = (sampleRate * durationSec).toInt()
        return ShortArray(n)
    }

    /**
     * Convert raw PCM 16-bit mono samples to a WAV byte array.
     * @param pcm PCM samples
     * @param sampleRate Sample rate
     * @return Complete WAV file bytes
     */
    fun pcmToWav(pcm: ShortArray, sampleRate: Int): ByteArray {
        val b = ByteArrayOutputStream()
        val dataSize = pcm.size * 2
        // RIFF header
        b.write("RIFF".toByteArray())
        writeLEInt(b, 36 + dataSize)
        b.write("WAVE".toByteArray())
        // fmt chunk
        b.write("fmt ".toByteArray())
        writeLEInt(b, 16)                    // chunk size
        writeLEShort(b, 1)                   // PCM format
        writeLEShort(b, 1)                   // mono
        writeLEInt(b, sampleRate)
        writeLEInt(b, sampleRate * 2)        // byte rate (mono 16-bit)
        writeLEShort(b, 2)                   // block align
        writeLEShort(b, 16)                  // bits per sample
        // data chunk
        b.write("data".toByteArray())
        writeLEInt(b, dataSize)
        for (s in pcm) writeLEShort(b, s.toInt())
        return b.toByteArray()
    }

    /**
     * Validate WAV header structure. Returns parsed metadata or null if invalid.
     */
    fun parseWavHeader(wav: ByteArray): WavInfo? {
        if (wav.size < 44) return null
        val riff = String(wav, 0, 4)
        val wave = String(wav, 8, 4)
        if (riff != "RIFF" || wave != "WAVE") return null

        val fmt = String(wav, 12, 4)
        if (fmt != "fmt ") return null

        val audioFormat = readLEShort(wav, 20)
        val numChannels = readLEShort(wav, 22)
        val sampleRate = readLEInt(wav, 24)
        val bitsPerSample = readLEShort(wav, 34)
        val dataSize = readLEInt(wav, 40)

        return WavInfo(audioFormat, numChannels, sampleRate, bitsPerSample, dataSize)
    }

    /** Parsed WAV metadata */
    data class WavInfo(
        val audioFormat: Int,
        val numChannels: Int,
        val sampleRate: Int,
        val bitsPerSample: Int,
        val dataSize: Int
    )

    /** Pitch step table: 13 entries from unison (1.0) to octave (2.0), one per semitone */
    val PITCH_STEPS = floatArrayOf(
        1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
        1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
    )

    /** Map combo (1-based) to pitch multiplier */
    fun pitchForCombo(combo: Int): Float {
        val index = (combo - 1).coerceIn(0, PITCH_STEPS.size - 1)
        return PITCH_STEPS[index]
    }

    // --- Internal helpers ---

    internal fun writeLEInt(b: ByteArrayOutputStream, v: Int) {
        b.write(v and 0xFF)
        b.write((v shr 8) and 0xFF)
        b.write((v shr 16) and 0xFF)
        b.write((v shr 24) and 0xFF)
    }

    internal fun writeLEShort(b: ByteArrayOutputStream, v: Int) {
        b.write(v and 0xFF)
        b.write((v shr 8) and 0xFF)
    }

    private fun readLEInt(buf: ByteArray, off: Int): Int {
        return (buf[off].toInt() and 0xFF) or
                ((buf[off + 1].toInt() and 0xFF) shl 8) or
                ((buf[off + 2].toInt() and 0xFF) shl 16) or
                ((buf[off + 3].toInt() and 0xFF) shl 24)
    }

    private fun readLEShort(buf: ByteArray, off: Int): Int {
        return (buf[off].toInt() and 0xFF) or ((buf[off + 1].toInt() and 0xFF) shl 8)
    }
}
