package com.xarlord.numbertap.ui

import android.content.Context
import android.os.Vibrator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * Tests for HapticFeedback — #156.
 *
 * Verifies that each haptic method correctly retrieves the Vibrator service
 * and calls vibrate(). Unit tests run against android.jar stubs (SDK < O),
 * so the deprecated vibrate(milliseconds) path is exercised.
 *
 * comboBuzz uses the waveform overload which isn't in the stubs — we test
 * that it doesn't crash instead of verifying the exact call.
 */
@Suppress("DEPRECATION")
class HapticFeedbackTest {

    private val mockVibrator: Vibrator = mockk(relaxed = true)
    private val mockContext: Context = mockk {
        every { getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
    }

    @Test
    fun `lightClick calls vibrate on vibrator`() {
        HapticFeedback.lightClick(mockContext)
        verify { mockVibrator.vibrate(any<Long>()) }
    }

    @Test
    fun `mediumClick calls vibrate on vibrator`() {
        HapticFeedback.mediumClick(mockContext)
        verify { mockVibrator.vibrate(any<Long>()) }
    }

    @Test
    fun `errorBuzz calls vibrate on vibrator`() {
        HapticFeedback.errorBuzz(mockContext)
        verify { mockVibrator.vibrate(any<Long>()) }
    }

    @Test
    fun `comboBuzz does not crash when vibrator is available`() {
        // comboBuzz calls a waveform overload not present in unit-test stubs,
        // but the relaxed mock swallows it. Verify no exception thrown.
        HapticFeedback.comboBuzz(mockContext)
    }

    @Test
    fun `gameOverBuzz calls vibrate on vibrator`() {
        HapticFeedback.gameOverBuzz(mockContext)
        verify { mockVibrator.vibrate(any<Long>()) }
    }

    @Test
    fun `lightClick does not crash when vibrator is null`() {
        val nullVibratorContext: Context = mockk {
            every { getSystemService(Context.VIBRATOR_SERVICE) } returns null
        }
        HapticFeedback.lightClick(nullVibratorContext)
    }

    @Test
    fun `gameOverBuzz does not crash when vibrator is null`() {
        val nullVibratorContext: Context = mockk {
            every { getSystemService(Context.VIBRATOR_SERVICE) } returns null
        }
        HapticFeedback.gameOverBuzz(nullVibratorContext)
    }

    @Test
    fun `comboBuzz does not crash when vibrator is null`() {
        val nullVibratorContext: Context = mockk {
            every { getSystemService(Context.VIBRATOR_SERVICE) } returns null
        }
        HapticFeedback.comboBuzz(nullVibratorContext)
    }
}
