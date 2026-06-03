package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.ActionType
import com.xarlord.numbertap.data.GameAction
import org.junit.Assert.*
import org.junit.Test

class ActionLoggerTest {

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
}
