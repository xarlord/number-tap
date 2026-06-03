package com.xarlord.numbertap.audio

import android.content.Context
import android.media.SoundPool

class SoundManager(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(3).build()
    private var successSoundId: Int = 0
    private var failureSoundId: Int = 0
    private var basePitch = 1.0f

    // Pitch table for combo half-steps (semitone multiplier)
    private val pitchSteps = floatArrayOf(
        1.0f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f,
        1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8877f, 2.0f
    )

    fun playSuccess(combo: Int) {
        val pitch = pitchSteps[minOf(combo, pitchSteps.size - 1)]
        if (successSoundId != 0) {
            soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, pitch)
        }
    }

    fun playFailure() {
        if (failureSoundId != 0) {
            soundPool.play(failureSoundId, 1.0f, 1.0f, 1, 0, 0.5f)
        }
    }

    fun release() {
        soundPool.release()
    }

    fun getPitchForCombo(combo: Int): Float = pitchSteps[minOf(combo, pitchSteps.size - 1)]

    companion object {
        const val TAG = "SoundManager"
    }
}
