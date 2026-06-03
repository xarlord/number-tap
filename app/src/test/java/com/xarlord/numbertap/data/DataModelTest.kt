package com.xarlord.numbertap.data

import org.junit.Assert.*
import org.junit.Test

class DifficultyConfigTest {

    @Test
    fun `tier for score 0 returns 4x4 easy`() {
        val tier = DifficultyConfig.tierForScore(0)
        assertEquals(4, tier.gridRows)
        assertEquals(4, tier.gridCols)
        assertEquals(16, tier.maxSpawnedValue)
        assertEquals(1.0, tier.timeGainSeconds, 0.01)
        assertEquals(1.5, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `tier for score 15 returns 4x4 easy`() {
        val tier = DifficultyConfig.tierForScore(15)
        assertEquals(4, tier.gridRows)
        assertEquals(1.0, tier.timeGainSeconds, 0.01)
    }

    @Test
    fun `tier for score 16 returns 4x4 medium`() {
        val tier = DifficultyConfig.tierForScore(16)
        assertEquals(4, tier.gridRows)
        assertEquals(32, tier.maxSpawnedValue)
        assertEquals(0.7, tier.timeGainSeconds, 0.01)
        assertEquals(2.0, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `tier for score 40 returns 4x4 medium`() {
        val tier = DifficultyConfig.tierForScore(40)
        assertEquals(4, tier.gridRows)
        assertEquals(0.7, tier.timeGainSeconds, 0.01)
    }

    @Test
    fun `tier for score 41 returns 5x5 hard`() {
        val tier = DifficultyConfig.tierForScore(41)
        assertEquals(5, tier.gridRows)
        assertEquals(5, tier.gridCols)
        assertEquals(50, tier.maxSpawnedValue)
        assertEquals(0.5, tier.timeGainSeconds, 0.01)
        assertEquals(3.0, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `tier for score 100 returns 5x5 hard`() {
        val tier = DifficultyConfig.tierForScore(100)
        assertEquals(5, tier.gridRows)
        assertEquals(0.5, tier.timeGainSeconds, 0.01)
    }
}

class TileTest {

    @Test
    fun `tile default state is ACTIVE`() {
        val tile = Tile(id = 0, currentValue = 5)
        assertEquals(TileState.ACTIVE, tile.state)
        assertFalse(tile.isTarget)
    }

    @Test
    fun `tile copy preserves id`() {
        val tile = Tile(id = 3, currentValue = 7, state = TileState.TAPPED_CORRECT)
        val copy = tile.copy(currentValue = 99)
        assertEquals(3, copy.id)
        assertEquals(99, copy.currentValue)
        assertEquals(TileState.TAPPED_CORRECT, copy.state)
    }
}

class GameStateTest {

    @Test
    fun `default game state is not playing`() {
        val state = GameState()
        assertFalse(state.isPlaying)
        assertFalse(state.isGameOver)
        assertEquals(1, state.targetNumber)
        assertEquals(0, state.score)
        assertEquals(30.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `game state copy updates correctly`() {
        val state = GameState(score = 10, timeRemaining = 15.5)
        val updated = state.copy(score = 11, timeRemaining = 16.2)
        assertEquals(11, updated.score)
        assertEquals(16.2, updated.timeRemaining, 0.01)
        assertEquals(10, state.score) // original unchanged
    }
}

class GameActionTest {

    @Test
    fun `action type enum has all expected values`() {
        val types = ActionType.values()
        assertEquals(5, types.size)
        assertTrue(types.contains(ActionType.TAP_CORRECT))
        assertTrue(types.contains(ActionType.TAP_WRONG))
        assertTrue(types.contains(ActionType.GAME_START))
        assertTrue(types.contains(ActionType.GAME_OVER))
        assertTrue(types.contains(ActionType.GRID_TRANSITION))
    }

    @Test
    fun `game action defaults are set`() {
        val action = GameAction(timestamp = 1000L, type = ActionType.TAP_CORRECT)
        assertEquals(-1, action.tileRow)
        assertEquals(-1, action.tileCol)
        assertEquals(-1, action.tileValue)
        assertEquals(0, action.score)
        assertEquals(0.0, action.timeRemaining, 0.01)
    }
}
