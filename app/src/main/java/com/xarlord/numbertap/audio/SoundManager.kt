package com.xarlord.numbertap.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.ByteArrayOutputStream
import java.io.File

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

    // #52: Track temp files for cleanup
    private val tempFiles = mutableListOf<File>()

    private val pitchSteps = floatArrayOf(
        1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
        1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
    )

    init {
        val successFile = createTempWav(context, pcmToWav(generateSineWave(1000.0, 0.1, 44100), 44100), "nt_success.wav")
        successSoundId = soundPool.load(successFile.absolutePath, 1)

        val failureFile = createTempWav(context, pcmToWav(generateSineWave(150.0, 0.2, 44100), 44100), "nt_failure.wav")
        failureSoundId = soundPool.load(failureFile.absolutePath, 1)
    }

    fun playSuccess(combo: Int) {
        if (isReleased) return
        val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
        if (successSoundId != 0) soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, pitch)
    }

    fun playFailure() {
        if (isReleased) return
        if (failureSoundId != 0) soundPool.play(failureSoundId, 1.0f, 1.0f, 1, 0, 0.5f)
    }

    fun release() {
        if (!isReleased) {
            soundPool.release()
            tempFiles.forEach { it.delete() }
            tempFiles.clear()
            isReleased = true
        }
    }

    private fun generateSineWave(freqHz: Double, durationSec: Double, sampleRate: Int): ShortArray {
        val n = (sampleRate * durationSec).toInt()
        val samples = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            val env = if (i > n * 0.8) (n - i).toDouble() / (n * 0.2) else 1.0
            samples[i] = (Math.sin(2.0 * Math.PI * freqHz * t) * Short.MAX_VALUE * 0.5 * env).toInt().toShort()
        }
        return samples
    }

    private fun pcmToWav(pcm: ShortArray, sr: Int): ByteArray {
        val b = ByteArrayOutputStream()
        val ds = pcm.size * 2
        b.write("RIFF".toByteArray()); writeLEInt(b, 36 + ds); b.write("WAVE".toByteArray())
        b.write("fmt ".toByteArray()); writeLEInt(b, 16); writeLEShort(b, 1); writeLEShort(b, 1)
        writeLEInt(b, sr); writeLEInt(b, sr * 2); writeLEShort(b, 2); writeLEShort(b, 16)
        b.write("data".toByteArray()); writeLEInt(b, ds)
        for (s in pcm) writeLEShort(b, s.toInt())
        return b.toByteArray()
    }

    private fun writeLEInt(b: ByteArrayOutputStream, v: Int) { b.write(v and 0xFF); b.write((v shr 8) and 0xFF); b.write((v shr 16) and 0xFF); b.write((v shr 24) and 0xFF) }
    private fun writeLEShort(b: ByteArrayOutputStream, v: Int) { b.write(v and 0xFF); b.write((v shr 8) and 0xFF) }

    private fun createTempWav(context: Context, data: ByteArray, name: String): File {
        val f = File(context.cacheDir, name)
        f.writeBytes(data)
        tempFiles.add(f)
        return f
    }
}
