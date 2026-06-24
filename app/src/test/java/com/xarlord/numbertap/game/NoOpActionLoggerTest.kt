package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.GameAction
import com.xarlord.numbertap.data.ActionType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NoOpActionLogger.
 * #239: Added to maintain ≥60% coverage after dead-code cleanup.
 * Each method is a no-op; tests verify no exception and interface compliance.
 */
class NoOpActionLoggerTest {

    private val logger = NoOpActionLogger()

    @Test
    fun `log does not throw`() {
        logger.log(GameAction(timestamp = 0L, type = ActionType.TAP_CORRECT))
    }

    @Test
    fun `logGameStart does not throw`() {
        logger.logGameStart(score = 10, highScore = 100)
    }

    @Test
    fun `logTap does not throw`() {
        logger.logTap(row = 0, col = 0, value = 1, target = 1, correct = true, score = 5, time = 25.0)
    }

    @Test
    fun `logGameOver does not throw`() {
        logger.logGameOver(score = 50, highScore = 100, time = 0.0)
    }

    @Test
    fun `logGridTransition does not throw`() {
        logger.logGridTransition(score = 16, newSize = 25)
    }

    @Test
    fun `logTutorialStart does not throw`() {
        logger.logTutorialStart()
    }

    @Test
    fun `logTutorialComplete does not throw`() {
        logger.logTutorialComplete(score = 5)
    }

    @Test
    fun `logPause does not throw`() {
        logger.logPause(score = 10, time = 20.0)
    }

    @Test
    fun `logResume does not throw`() {
        logger.logResume(score = 10, time = 20.0)
    }

    @Test
    fun `logRevive does not throw`() {
        logger.logRevive(score = 10, time = 5.0)
    }

    @Test
    fun `logScoreMilestone does not throw`() {
        logger.logScoreMilestone(score = 50, label = "HALF CENTURY")
    }

    @Test
    fun `logShare does not throw`() {
        logger.logShare(score = 50)
    }

    @Test
    fun `logTierAnnouncement does not throw`() {
        logger.logTierAnnouncement(score = 16, tier = "MEDIUM")
    }

    @Test
    fun `logError does not throw`() {
        logger.logError(location = "Test", message = "test error")
    }

    @Test
    fun `implements ActionLoggerProvider`() {
        assertTrue(logger is ActionLoggerProvider)
    }
}
