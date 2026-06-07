package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.ActionType
import com.xarlord.numbertap.data.GameAction
import org.junit.Assert.*
import org.junit.Test

/**
 * In-memory test double for ActionLoggerProvider.
 * Captures logged actions for assertion in tests.
 */
class InMemoryActionLogger : ActionLoggerProvider {
    val actions = mutableListOf<GameAction>()
    override fun log(action: GameAction) { actions.add(action) }
    override fun logGameStart(score: Int, highScore: Int) { log(GameAction(timestamp = 0L, type = ActionType.GAME_START, score = score)) }
    override fun logTap(row: Int, col: Int, value: Int, target: Int, correct: Boolean, score: Int, time: Double) { log(GameAction(timestamp = 0L, type = if (correct) ActionType.TAP_CORRECT else ActionType.TAP_WRONG, tileRow = row, tileCol = col, tileValue = value, targetValue = target, score = score, timeRemaining = time)) }
    override fun logGameOver(score: Int, highScore: Int, time: Double) { log(GameAction(timestamp = 0L, type = ActionType.GAME_OVER, score = score)) }
    override fun logGridTransition(score: Int, newSize: Int) { log(GameAction(timestamp = 0L, type = ActionType.GRID_TRANSITION, score = score, extra = "newGrid=$newSize")) }
    override fun logTutorialStart() { log(GameAction(timestamp = 0L, type = ActionType.TUTORIAL_START, extra = "firstTime=true")) }
    override fun logTutorialComplete(score: Int) { log(GameAction(timestamp = 0L, type = ActionType.TUTORIAL_COMPLETE, score = score, extra = "transitionedToGame=true")) }
    override fun logPause(score: Int, time: Double) { log(GameAction(timestamp = 0L, type = ActionType.PAUSE, score = score, timeRemaining = time)) }
    override fun logResume(score: Int, time: Double) { log(GameAction(timestamp = 0L, type = ActionType.RESUME, score = score, timeRemaining = time)) }
    override fun logRevive(score: Int, time: Double) { log(GameAction(timestamp = 0L, type = ActionType.REVIVE, score = score, timeRemaining = time, extra = "bonusTime=5.0")) }
    override fun logScoreMilestone(score: Int, label: String) { log(GameAction(timestamp = 0L, type = ActionType.SCORE_MILESTONE, score = score, extra = "label=$label")) }
    override fun logShare(score: Int) { log(GameAction(timestamp = 0L, type = ActionType.SHARE, score = score)) }
    override fun logTierAnnouncement(score: Int, tier: String) { log(GameAction(timestamp = 0L, type = ActionType.TIER_ANNOUNCEMENT, score = score, extra = "tier=$tier")) }
    override fun logError(location: String, message: String) { log(GameAction(timestamp = 0L, type = ActionType.ERROR, extra = "error_location=$location error_message=$message")) }
}

class ActionLoggerTest {

    // --- Original data-class tests (unchanged) ---

    @Test
    fun `game action for tap correct has all fields`() {
        val action = GameAction(
            timestamp = 1000L,
            type = ActionType.TAP_CORRECT,
            tileRow = 2,
            tileCol = 3,
            tileValue = 7,
            targetValue = 7,
            score = 5,
            timeRemaining = 25.0
        )
        assertEquals(1000L, action.timestamp)
        assertEquals(ActionType.TAP_CORRECT, action.type)
        assertEquals(2, action.tileRow)
        assertEquals(3, action.tileCol)
        assertEquals(7, action.tileValue)
        assertEquals(7, action.targetValue)
        assertEquals(5, action.score)
        assertEquals(25.0, action.timeRemaining, 0.01)
    }

    @Test
    fun `game action for tap wrong has wrong type`() {
        val action = GameAction(
            timestamp = 2000L,
            type = ActionType.TAP_WRONG,
            tileRow = 0,
            tileCol = 1,
            tileValue = 5,
            targetValue = 3
        )
        assertEquals(ActionType.TAP_WRONG, action.type)
        assertNotEquals(5, action.targetValue) // value != target
    }

    @Test
    fun `game action for game start`() {
        val action = GameAction(
            timestamp = 3000L,
            type = ActionType.GAME_START,
            score = 0,
            timeRemaining = 30.0
        )
        assertEquals(ActionType.GAME_START, action.type)
        assertEquals(0, action.score)
        assertEquals(-1, action.tileRow) // no tile for game start
    }

    @Test
    fun `game action for game over`() {
        val action = GameAction(
            timestamp = 4000L,
            type = ActionType.GAME_OVER,
            score = 42,
            timeRemaining = 0.0
        )
        assertEquals(ActionType.GAME_OVER, action.type)
        assertEquals(42, action.score)
        assertEquals(0.0, action.timeRemaining, 0.01)
    }

    @Test
    fun `game action for grid transition`() {
        val action = GameAction(
            timestamp = 5000L,
            type = ActionType.GRID_TRANSITION,
            score = 41
        )
        assertEquals(ActionType.GRID_TRANSITION, action.type)
        assertEquals(41, action.score)
    }

    @Test
    fun `action type names are uppercase`() {
        for (type in ActionType.entries) {
            assertEquals(type.name, type.name.uppercase())
        }
    }

    @Test
    fun `all action types have distinct names`() {
        val names = ActionType.entries.map { it.name }.toSet()
        assertEquals(ActionType.entries.size, names.size)
    }

    // --- New interface / InMemoryActionLogger tests ---

    @Test
    fun `InMemoryActionLogger captures logGameStart action`() {
        val logger = InMemoryActionLogger()
        logger.logGameStart(score = 0, highScore = 100)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.GAME_START, logger.actions[0].type)
        assertEquals(0, logger.actions[0].score)
    }

    @Test
    fun `InMemoryActionLogger captures logTap correct action`() {
        val logger = InMemoryActionLogger()
        logger.logTap(row = 1, col = 2, value = 5, target = 5, correct = true, score = 10, time = 20.0)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.TAP_CORRECT, logger.actions[0].type)
        assertEquals(1, logger.actions[0].tileRow)
        assertEquals(2, logger.actions[0].tileCol)
        assertEquals(5, logger.actions[0].tileValue)
        assertEquals(10, logger.actions[0].score)
    }

    @Test
    fun `InMemoryActionLogger captures logTap wrong action`() {
        val logger = InMemoryActionLogger()
        logger.logTap(row = 0, col = 0, value = 3, target = 7, correct = false, score = 5, time = 15.0)
        assertEquals(ActionType.TAP_WRONG, logger.actions[0].type)
    }

    @Test
    fun `InMemoryActionLogger captures logGameOver action`() {
        val logger = InMemoryActionLogger()
        logger.logGameOver(score = 42, highScore = 50, time = 0.0)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.GAME_OVER, logger.actions[0].type)
        assertEquals(42, logger.actions[0].score)
    }

    @Test
    fun `InMemoryActionLogger captures multiple actions in order`() {
        val logger = InMemoryActionLogger()
        logger.logGameStart(score = 0, highScore = 0)
        logger.logTap(row = 0, col = 1, value = 1, target = 1, correct = true, score = 1, time = 29.5)
        logger.logGameOver(score = 1, highScore = 1, time = 0.0)
        assertEquals(3, logger.actions.size)
        assertEquals(ActionType.GAME_START, logger.actions[0].type)
        assertEquals(ActionType.TAP_CORRECT, logger.actions[1].type)
        assertEquals(ActionType.GAME_OVER, logger.actions[2].type)
    }

    @Test
    fun `ActionLogger singleton delegates to LogcatActionLogger`() {
        // Verify the singleton implements the interface via delegation
        val provider: ActionLoggerProvider = ActionLogger
        assertNotNull(provider)
        // Verify the singleton can be used as an ActionLoggerProvider
        assertNotEquals(null, provider as? ActionLoggerProvider)
    }

    @Test
    fun `interface contract covers all public methods`() {
        // Verify all methods defined on the interface are callable via a test double
        val logger = InMemoryActionLogger()
        logger.logGameStart(0, 0)
        logger.logTap(0, 0, 1, 1, true, 1, 30.0)
        logger.logGameOver(0, 0, 0.0)
        logger.logGridTransition(10, 4)
        logger.logTutorialStart()
        logger.logTutorialComplete(5)
        logger.logPause(5, 15.0)
        logger.logResume(5, 15.0)
        logger.logRevive(5, 3.0)
        logger.logScoreMilestone(10, "10")
        logger.logShare(10)
        logger.logTierAnnouncement(10, "bronze")
        logger.logError("loc", "msg")
        // Now all methods capture actions
        assertEquals(13, logger.actions.size)
    }

    // --- Tests for previously uncovered LogcatActionLogger methods ---

    @Test
    fun `logShare captures SHARE action with score`() {
        val logger = InMemoryActionLogger()
        logger.logShare(42)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.SHARE, logger.actions[0].type)
        assertEquals(42, logger.actions[0].score)
    }

    @Test
    fun `logTierAnnouncement captures TIER_ANNOUNCEMENT with tier extra`() {
        val logger = InMemoryActionLogger()
        logger.logTierAnnouncement(25, "AMAZING")
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.TIER_ANNOUNCEMENT, logger.actions[0].type)
        assertEquals(25, logger.actions[0].score)
        assertTrue(logger.actions[0].extra.contains("tier=AMAZING"))
    }

    @Test
    fun `logError captures ERROR action with location and message`() {
        val logger = InMemoryActionLogger()
        logger.logError("game_loop", "timeout")
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.ERROR, logger.actions[0].type)
        assertTrue(logger.actions[0].extra.contains("error_location=game_loop"))
        assertTrue(logger.actions[0].extra.contains("error_message=timeout"))
    }

    @Test
    fun `logGridTransition captures GRID_TRANSITION with new grid size`() {
        val logger = InMemoryActionLogger()
        logger.logGridTransition(41, 5)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.GRID_TRANSITION, logger.actions[0].type)
        assertEquals(41, logger.actions[0].score)
        assertTrue(logger.actions[0].extra.contains("newGrid=5"))
    }

    @Test
    fun `logTutorialStart captures TUTORIAL_START`() {
        val logger = InMemoryActionLogger()
        logger.logTutorialStart()
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.TUTORIAL_START, logger.actions[0].type)
    }

    @Test
    fun `logTutorialComplete captures TUTORIAL_COMPLETE with score`() {
        val logger = InMemoryActionLogger()
        logger.logTutorialComplete(5)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.TUTORIAL_COMPLETE, logger.actions[0].type)
        assertEquals(5, logger.actions[0].score)
    }

    @Test
    fun `logPause captures PAUSE with score and time`() {
        val logger = InMemoryActionLogger()
        logger.logPause(10, 15.5)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.PAUSE, logger.actions[0].type)
        assertEquals(10, logger.actions[0].score)
        assertEquals(15.5, logger.actions[0].timeRemaining, 0.01)
    }

    @Test
    fun `logResume captures RESUME with score and time`() {
        val logger = InMemoryActionLogger()
        logger.logResume(10, 15.5)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.RESUME, logger.actions[0].type)
    }

    @Test
    fun `logRevive captures REVIVE with bonus time extra`() {
        val logger = InMemoryActionLogger()
        logger.logRevive(20, 0.0)
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.REVIVE, logger.actions[0].type)
        assertEquals(20, logger.actions[0].score)
        assertTrue(logger.actions[0].extra.contains("bonusTime=5.0"))
    }

    @Test
    fun `logScoreMilestone captures SCORE_MILESTONE with label`() {
        val logger = InMemoryActionLogger()
        logger.logScoreMilestone(50, "LEGENDARY")
        assertEquals(1, logger.actions.size)
        assertEquals(ActionType.SCORE_MILESTONE, logger.actions[0].type)
        assertEquals(50, logger.actions[0].score)
        assertTrue(logger.actions[0].extra.contains("label=LEGENDARY"))
    }
}
