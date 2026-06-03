package com.xarlord.numbertap.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import com.xarlord.numbertap.data.GameState
import java.io.ByteArrayOutputStream

/**
 * SoundPool-based audio manager with programmatically generated sound effects.
 * Generates success ping (800-1200Hz) and failure thud (150Hz) at init time.
 */
class SoundManager(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var successSoundId: Int = 0
    private var failureSoundId: Int = 0
    private var isReleased = false

    // Pitch table for combo half-steps (semitone multiplier)
    private val pitchSteps = floatArrayOf(
        1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
        1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
    )

    init {
        // Generate success ping: 1000Hz sine wave, 100ms
        val successPcm = generateSineWave(1000.0, 0.1, 44100)
        val successWav = pcmToWav(successPcm, 44100)
        successSoundId = soundPool.load(
            createTempFileFromPcm(context, successWav, "success.wav"),
            1
        )

        // Generate failure thud: 150Hz sine wave, 200ms
        val failurePcm = generateSineWave(150.0, 0.2, 44100)
        val failureWav = pcmToWav(failurePcm, 44100)
        failureSoundId = soundPool.load(
            createTempFileFromPcm(context, failureWav, "failure.wav"),
            1
        )
    }

    fun playSuccess(combo: Int) {
        if (isReleased) return
        val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
        if (successSoundId != 0) {
            soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, pitch)
        }
    }

    fun playFailure() {
        if (isReleased) return
        if (failureSoundId != 0) {
            soundPool.play(failureSoundId, 1.0f, 1.0f, 1, 0, 0.5f)
        }
    }

    fun release() {
        if (!isReleased) {
            soundPool.release()
            isReleased = true
        }
    }

    fun getPitchForCombo(combo: Int): Float = pitchSteps[minOf(combo, pitchSteps.size - 1)]

    private fun generateSineWave(freqHz: Double, durationSec: Double, sampleRate: Int): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Apply envelope (fade out in last 20%)
            val envelope = if (i > numSamples * 0.8) {
                (numSamples - i).toDouble() / (numSamples * 0.2)
            } else 1.0
            val value = Math.sin(2.0 * Math.PI * freqHz * t) * Short.MAX_VALUE * 0.5 * envelope
            samples[i] = value.toInt().toShort()
        }
        return samples
    }

    private fun pcmToWav(pcmData: ShortArray, sampleRate: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize

        // RIFF header
        baos.write("RIFF".toByteArray())
        writeLittleEndianInt(baos, totalSize)
        baos.write("WAVE".toByteArray())

        // fmt chunk
        baos.write("fmt ".toByteArray())
        writeLittleEndianInt(baos, 16) // chunk size
        writeLittleEndianShort(baos, 1) // PCM format
        writeLittleEndianShort(baos, 1) // mono
        writeLittleEndianInt(baos, sampleRate)
        writeLittleEndianInt(baos, sampleRate * 2) // byte rate
        writeLittleEndianShort(baos, 2) // block align
        writeLittleEndianShort(baos, 16) // bits per sample

        // data chunk
        baos.write("data".toByteArray())
        writeLittleEndianInt(baos, dataSize)
        for (sample in pcmData) {
            writeLittleEndianShort(baos, sample.toInt())
        }

        return baos.toByteArray()
    }

    private fun writeLittleEndianInt(baos: ByteArrayOutputStream, value: Int) {
        baos.write(value and 0xFF)
        baos.write((value shr 8) and 0xFF)
        baos.write((value shr 16) and 0xFF)
        baos.write((value shr 24) and 0xFF)
    }

    private fun writeLittleEndianShort(baos: ByteArrayOutputStream, value: Int) {
        baos.write(value and 0xFF)
        baos.write((value shr 8) and 0xFF)
    }

    private fun createTempFileFromPcm(context: Context, wavData: ByteArray, fileName: String): String {
        val tempFile = java.io.File(context.cacheDir, fileName)
        tempFile.writeBytes(wavData)
        return tempFile.absolutePath
    }

    companion object {
        private const val TAG = "SoundManager"
    }
}
