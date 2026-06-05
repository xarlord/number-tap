package com.xarlord.numbertap.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import java.io.ByteArrayOutputStream
import java.io.File

class SoundManager(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var successSoundId: Int = 0
    private var failureSoundId: Int = 0
    private var tickSoundId: Int = 0
    private var gameOverSoundId: Int = 0
    private var milestoneSoundId: Int = 0
    private var comboBreakSoundId: Int = 0
    private var isReleased = false

    private val tempFiles = mutableListOf<File>()

    private val pitchSteps = floatArrayOf(
        1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
        1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
    )

    // Background music state
    private var bgMusicTrack: AudioTrack? = null
    private var isMusicPlaying = false

    init {
        // Success: high ping 1000Hz, 0.1s
        val successFile = createTempWav(context, pcmToWav(generateSineWave(1000.0, 0.1, 44100), 44100), "nt_success.wav")
        successSoundId = soundPool.load(successFile.absolutePath, 1)

        // Failure: low thud 150Hz, 0.2s
        val failureFile = createTempWav(context, pcmToWav(generateSineWave(150.0, 0.2, 44100), 44100), "nt_failure.wav")
        failureSoundId = soundPool.load(failureFile.absolutePath, 1)

        // Countdown tick: soft click 600Hz, 0.03s
        val tickFile = createTempWav(context, pcmToWav(generateSineWave(600.0, 0.03, 44100), 44100), "nt_tick.wav")
        tickSoundId = soundPool.load(tickFile.absolutePath, 1)

        // Game over: descending tone 400Hz, 0.5s
        val gameOverFile = createTempWav(context, pcmToWav(generateSineWave(400.0, 0.5, 44100), 44100), "nt_gameover.wav")
        gameOverSoundId = soundPool.load(gameOverFile.absolutePath, 1)

        // Milestone: bright chime 1200Hz, 0.15s
        val milestoneFile = createTempWav(context, pcmToWav(generateSineWave(1200.0, 0.15, 44100), 44100), "nt_milestone.wav")
        milestoneSoundId = soundPool.load(milestoneFile.absolutePath, 1)

        // Combo break: 200Hz, 0.1s
        val comboBreakFile = createTempWav(context, pcmToWav(generateSineWave(200.0, 0.1, 44100), 44100), "nt_combobreak.wav")
        comboBreakSoundId = soundPool.load(comboBreakFile.absolutePath, 1)
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

    fun playCountdownTick() {
        if (isReleased) return
        if (tickSoundId != 0) soundPool.play(tickSoundId, 0.4f, 0.4f, 1, 0, 1.0f)
    }

    fun playGameOver() {
        if (isReleased) return
        if (gameOverSoundId != 0) soundPool.play(gameOverSoundId, 1.0f, 1.0f, 1, 0, 0.7f)
    }

    fun playMilestone() {
        if (isReleased) return
        if (milestoneSoundId != 0) soundPool.play(milestoneSoundId, 0.8f, 0.8f, 1, 0, 1.2f)
    }

    fun playComboBreak() {
        if (isReleased) return
        if (comboBreakSoundId != 0) soundPool.play(comboBreakSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    /** Start procedural background music — simple looping beat */
    fun startBGMusic() {
        if (isReleased || isMusicPlaying) return
        try {
            val sampleRate = 22050
            val bpm = 130
            val beatDuration = 60.0 / bpm
            val totalBeats = 16 // 4 bars
            val totalSamples = (sampleRate * beatDuration * totalBeats).toInt()

            val samples = ShortArray(totalSamples)
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val beatPos = (t / beatDuration) % 4.0
                val measureBeat = (t / beatDuration).toInt() % 4

                // Kick on beats 0 and 2
                val kick = if (measureBeat == 0 || measureBeat == 2) {
                    val kickT = (t % beatDuration)
                    val kickFreq = 80.0 * (1.0 - kickT * 4).coerceAtLeast(0.2)
                    Math.sin(2.0 * Math.PI * kickFreq * t) * 0.3
                } else 0.0

                // Hi-hat on every beat
                val hihat = if (beatPos < 0.05) {
                    (Math.random() * 2 - 1) * 0.08
                } else 0.0

                // Bass note
                val bass = Math.sin(2.0 * Math.PI * 55.0 * t) * 0.15

                samples[i] = ((kick + hihat + bass) * Short.MAX_VALUE * 0.4).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(totalSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, totalSamples)
            @Suppress("DEPRECATION")
            track.setVolume(0.3f)
            track.play()
            bgMusicTrack = track
            isMusicPlaying = true
        } catch (_: Exception) {
            // Non-critical — game works without music
        }
    }

    fun stopBGMusic() {
        try {
            bgMusicTrack?.stop()
            bgMusicTrack?.release()
        } catch (_: Exception) {}
        bgMusicTrack = null
        isMusicPlaying = false
    }

    fun release() {
        if (!isReleased) {
            stopBGMusic()
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
