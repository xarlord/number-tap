package com.xarlord.numbertap.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import java.io.File
import kotlin.random.Random

class SoundManager(context: Context) : SoundManagerProvider {

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
    private val pendingDeleteFiles = mutableListOf<File>()
    private val totalSoundsToLoad = 6

    private val pitchSteps = AudioUtils.PITCH_STEPS

    // Background music state
    private var bgMusicTrack: AudioTrack? = null
    private var isMusicPlaying = false

    init {
        // Defer temp file cleanup until ALL SoundPool samples finish loading (fixes #117)
        val loadedCount = java.util.concurrent.atomic.AtomicInteger(0)
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            if (loadedCount.incrementAndGet() >= totalSoundsToLoad) {
                pendingDeleteFiles.forEach { it.delete() }
                pendingDeleteFiles.clear()
            }
        }

        // Success: high ping 1000Hz, 0.1s
        val successFile = createTempWav(context, AudioUtils.pcmToWav(AudioUtils.generateSineWave(1000.0, 0.1, 44100), 44100), "nt_success.wav")
        successSoundId = soundPool.load(successFile.absolutePath, 1)
        pendingDeleteFiles.add(successFile)

        // Failure: low thud 150Hz, 0.2s
        val failureFile = createTempWav(context, AudioUtils.pcmToWav(AudioUtils.generateSineWave(150.0, 0.2, 44100), 44100), "nt_failure.wav")
        failureSoundId = soundPool.load(failureFile.absolutePath, 1)
        pendingDeleteFiles.add(failureFile)

        // Countdown tick: soft click 600Hz, 0.03s
        val tickFile = createTempWav(context, AudioUtils.pcmToWav(AudioUtils.generateSineWave(600.0, 0.03, 44100), 44100), "nt_tick.wav")
        tickSoundId = soundPool.load(tickFile.absolutePath, 1)
        pendingDeleteFiles.add(tickFile)

        // Game over: descending tone 400Hz, 0.5s
        val gameOverFile = createTempWav(context, AudioUtils.pcmToWav(AudioUtils.generateSineWave(400.0, 0.5, 44100), 44100), "nt_gameover.wav")
        gameOverSoundId = soundPool.load(gameOverFile.absolutePath, 1)
        pendingDeleteFiles.add(gameOverFile)

        // Milestone: bright chime 1200Hz, 0.15s
        val milestoneFile = createTempWav(context, AudioUtils.pcmToWav(AudioUtils.generateSineWave(1200.0, 0.15, 44100), 44100), "nt_milestone.wav")
        milestoneSoundId = soundPool.load(milestoneFile.absolutePath, 1)
        pendingDeleteFiles.add(milestoneFile)

        // Combo break: 200Hz, 0.1s
        val comboBreakFile = createTempWav(context, AudioUtils.pcmToWav(AudioUtils.generateSineWave(200.0, 0.1, 44100), 44100), "nt_combobreak.wav")
        comboBreakSoundId = soundPool.load(comboBreakFile.absolutePath, 1)
        pendingDeleteFiles.add(comboBreakFile)
    }

    override fun playSuccess(combo: Int) {
        if (isReleased) return
        // GDD: first tap in sequence plays at base pitch; consecutive taps step up
        // combo is 1-based from GameEngine, so subtract 1 for pitch table index
        val pitchIndex = (combo - 1).coerceIn(0, pitchSteps.size - 1)
        val pitch = pitchSteps[pitchIndex]
        if (successSoundId != 0) soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, pitch)
    }

    override fun playFailure() {
        if (isReleased) return
        if (failureSoundId != 0) soundPool.play(failureSoundId, 1.0f, 1.0f, 1, 0, 0.5f)
    }

    override fun playCountdownTick() {
        if (isReleased) return
        if (tickSoundId != 0) soundPool.play(tickSoundId, 0.4f, 0.4f, 1, 0, 1.0f)
    }

    override fun playGameOver() {
        if (isReleased) return
        if (gameOverSoundId != 0) soundPool.play(gameOverSoundId, 1.0f, 1.0f, 1, 0, 0.7f)
    }

    override fun playMilestone() {
        if (isReleased) return
        if (milestoneSoundId != 0) soundPool.play(milestoneSoundId, 0.8f, 0.8f, 1, 0, 1.2f)
    }

    override fun playComboBreak() {
        if (isReleased) return
        if (comboBreakSoundId != 0) soundPool.play(comboBreakSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    /** Start procedural background music — simple looping beat */
    override fun startBGMusic() {
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
                    (Random.nextFloat() * 2 - 1) * 0.08
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

    override fun stopBGMusic() {
        try {
            bgMusicTrack?.stop()
            bgMusicTrack?.release()
        } catch (_: Exception) {}
        bgMusicTrack = null
        isMusicPlaying = false
    }

    override fun release() {
        if (!isReleased) {
            stopBGMusic()
            pendingDeleteFiles.forEach { it.delete() }
            pendingDeleteFiles.clear()
            soundPool.release()
            isReleased = true
        }
    }

    private fun createTempWav(context: Context, data: ByteArray, name: String): File {
        val f = File(context.cacheDir, name)
        f.writeBytes(data)
        return f
    }
}
