package com.xarlord.numbertap.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Lightweight haptic feedback utility for Number Tap.
 * Uses platform haptic constants for consistency across devices.
 */
object HapticFeedback {

    // Vibration durations (milliseconds)
    private const val LIGHT_DURATION_MS = 15L
    private const val MEDIUM_DURATION_MS = 25L
    private const val ERROR_DURATION_MS = 40L
    private const val GAME_OVER_DURATION_MS = 100L

    // Vibration amplitudes (0–255)
    private const val LIGHT_AMPLITUDE = 80
    private const val MEDIUM_AMPLITUDE = 120
    private const val ERROR_AMPLITUDE = 200
    private const val GAME_OVER_AMPLITUDE = 200
    private const val COMBO_RISE_AMPLITUDE = 100
    private const val COMBO_PEAK_AMPLITUDE = 150

    // Combo waveform pattern
    private val COMBO_TIMINGS = longArrayOf(0, 20, 30, 20)
    private val COMBO_AMPLITUDES = intArrayOf(0, COMBO_RISE_AMPLITUDE, 0, COMBO_PEAK_AMPLITUDE)
    private const val COMBO_REPEAT = -1 // no repeat

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /** Light tap — correct tile */
    fun lightClick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(LIGHT_DURATION_MS, LIGHT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(LIGHT_DURATION_MS)
        }
    }

    /** Medium tap — milestone (every 10 points) */
    fun mediumClick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(MEDIUM_DURATION_MS, MEDIUM_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(MEDIUM_DURATION_MS)
        }
    }

    /** Error/wrong tap — double buzz */
    fun errorBuzz(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ERROR_DURATION_MS, ERROR_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ERROR_DURATION_MS)
        }
    }

    /** Combo achieved — escalating pattern */
    fun comboBuzz(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(COMBO_TIMINGS, COMBO_AMPLITUDES, COMBO_REPEAT))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(COMBO_TIMINGS, COMBO_REPEAT)
        }
    }

    /** Game over — heavy */
    fun gameOverBuzz(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(GAME_OVER_DURATION_MS, GAME_OVER_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(GAME_OVER_DURATION_MS)
        }
    }
}
