package com.xarlord.numbertap.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Lightweight haptic feedback utility for Number Tap.
 * Uses platform haptic constants for consistency across devices.
 */
object HapticFeedback {

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
            vibrator.vibrate(VibrationEffect.createOneShot(15, 80))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }

    /** Medium tap — milestone (every 10 points) */
    fun mediumClick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, 120))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }

    /** Error/wrong tap — double buzz */
    fun errorBuzz(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, 200))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }

    /** Combo achieved — escalating pattern */
    fun comboBuzz(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 20, 30, 20)
            val amplitudes = intArrayOf(0, 100, 0, 150)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 20, 30, 20), -1)
        }
    }

    /** Game over — heavy */
    fun gameOverBuzz(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, 200))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}
