package com.xarlord.numbertap.data

import org.junit.Assert.*
import org.junit.Test

class DifficultyConfigTest {

    @Test
    fun `tier for score 0 returns 4x4 easy`() {
        val tier = DifficultyConfig.tierForScore(0)
        assertEquals(4, tier.gridRows)
        assertEquals(4, tier.gridCols)
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
    fun `tier for score 20 returns 4x4 flip`() {
        val tier = DifficultyConfig.tierForScore(20)
        assertEquals(4, tier.gridRows)
        assertEquals(4, tier.gridCols)
        assertEquals(0.8, tier.timeGainSeconds, 0.01)
        assertEquals(2.0, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `tier for score 40 returns 4x4 flip`() {
        val tier = DifficultyConfig.tierForScore(40)
        assertEquals(4, tier.gridRows)
        assertEquals(0.8, tier.timeGainSeconds, 0.01)
    }

    @Test
    fun `tier for score 50 returns 5x5 expert`() {
        val tier = DifficultyConfig.tierForScore(50)
        assertEquals(5, tier.gridRows)
        assertEquals(5, tier.gridCols)
        assertEquals(0.6, tier.timeGainSeconds, 0.01)
        assertEquals(2.5, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `tier for score 65 returns 5x5 expert`() {
        val tier = DifficultyConfig.tierForScore(65)
        assertEquals(5, tier.gridRows)
        assertEquals(0.6, tier.timeGainSeconds, 0.01)
    }

    @Test
    fun `tier for score 100 returns 6x6 insane`() {
        val tier = DifficultyConfig.tierForScore(100)
        assertEquals(6, tier.gridRows)
        assertEquals(6, tier.gridCols)
        assertEquals(0.5, tier.timeGainSeconds, 0.01)
        assertEquals(3.0, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `tier for score 150 returns 6x6 insane`() {
        val tier = DifficultyConfig.tierForScore(150)
        assertEquals(6, tier.gridRows)
        assertEquals(0.5, tier.timeGainSeconds, 0.01)
    }

    @Test
    fun `tier boundaries are consistent`() {
        val normal = DifficultyConfig.tierForScore(0)
        val flip = DifficultyConfig.tierForScore(20)
        val expert = DifficultyConfig.tierForScore(50)
        val insane = DifficultyConfig.tierForScore(100)

        // Time gain decreases as difficulty increases
        assertTrue(normal.timeGainSeconds > flip.timeGainSeconds)
        assertTrue(flip.timeGainSeconds > expert.timeGainSeconds)
        assertTrue(expert.timeGainSeconds > insane.timeGainSeconds)

        // Time penalty increases as difficulty increases
        assertTrue(normal.timePenaltySeconds < flip.timePenaltySeconds)
        assertTrue(flip.timePenaltySeconds < expert.timePenaltySeconds)
        assertTrue(expert.timePenaltySeconds < insane.timePenaltySeconds)
    }

    @Test
    fun `all tiers have square grids`() {
        for (score in listOf(0, 15, 20, 40, 50, 65, 100, 150)) {
            val tier = DifficultyConfig.tierForScore(score)
            assertEquals("Grid should be square at score $score", tier.gridRows, tier.gridCols)
        }
    }
}

class TileTest {

    @Test
    fun `tile default state is ACTIVE`() {
        val tile = Tile(id = 0, currentValue = 5)
        assertEquals(TileState.ACTIVE, tile.state)
    }

    @Test
    fun `tile copy preserves id`() {
        val tile = Tile(id = 3, currentValue = 7, state = TileState.TAPPED_CORRECT)
        val copy = tile.copy(currentValue = 99)
        assertEquals(3, copy.id)
        assertEquals(99, copy.currentValue)
        assertEquals(TileState.TAPPED_CORRECT, copy.state)
    }

    @Test
    fun `tile state transitions`() {
        val tile = Tile(id = 0, currentValue = 1)
        assertEquals(TileState.ACTIVE, tile.state)

        val correct = tile.copy(state = TileState.TAPPED_CORRECT)
        assertEquals(TileState.TAPPED_CORRECT, correct.state)

        val wrong = tile.copy(state = TileState.TAPPED_WRONG)
        assertEquals(TileState.TAPPED_WRONG, wrong.state)

        val reset = wrong.copy(state = TileState.ACTIVE)
        assertEquals(TileState.ACTIVE, reset.state)
    }

    @Test
    fun `tile equality works`() {
        val t1 = Tile(id = 0, currentValue = 5)
        val t2 = Tile(id = 0, currentValue = 5)
        assertEquals(t1, t2)
    }

    @Test
    fun `tile inequality by value`() {
        val t1 = Tile(id = 0, currentValue = 5)
        val t2 = Tile(id = 0, currentValue = 6)
        assertNotEquals(t1, t2)
    }

    @Test
    fun `tile inequality by state`() {
        val t1 = Tile(id = 0, currentValue = 5, state = TileState.ACTIVE)
        val t2 = Tile(id = 0, currentValue = 5, state = TileState.TAPPED_CORRECT)
        assertNotEquals(t1, t2)
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
        assertEquals(0, state.highScore)
        assertEquals(4, state.gridSize)
        assertEquals(0, state.comboCount)
        assertEquals(0L, state.lastCorrectTapTime)
        assertEquals(Pair(0f, 0f), state.shakeOffset)
    }

    @Test
    fun `game state copy updates correctly`() {
        val state = GameState(score = 10, timeRemaining = 15.5)
        val updated = state.copy(score = 11, timeRemaining = 16.2)
        assertEquals(11, updated.score)
        assertEquals(16.2, updated.timeRemaining, 0.01)
        assertEquals(10, state.score) // original unchanged
    }

    @Test
    fun `game state with tiles`() {
        val tiles = listOf(
            listOf(Tile(id = 0, currentValue = 1), Tile(id = 1, currentValue = 2)),
            listOf(Tile(id = 2, currentValue = 3), Tile(id = 3, currentValue = 4))
        )
        val state = GameState(tiles = tiles)
        assertEquals(2, state.tiles.size)
        assertEquals(2, state.tiles[0].size)
        assertEquals(1, state.tiles[0][0].currentValue)
    }

    @Test
    fun `shake offset can be set and cleared`() {
        val state = GameState(shakeOffset = Pair(5f, -3f))
        assertEquals(Pair(5f, -3f), state.shakeOffset)
        val cleared = state.copy(shakeOffset = Pair(0f, 0f))
        assertEquals(Pair(0f, 0f), cleared.shakeOffset)
    }

    @Test
    fun `game over state`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 42)
        assertFalse(state.isPlaying)
        assertTrue(state.isGameOver)
        assertEquals(42, state.score)
    }

    // --- #192: isHardMode / hiddenTileIds / INSANE_MODE tests ---

    @Test
    fun `game state isHardMode defaults to false`() {
        val state = GameState()
        assertFalse(state.isHardMode)
    }

    @Test
    fun `game state hiddenTileIds defaults to empty`() {
        val state = GameState()
        assertTrue(state.hiddenTileIds.isEmpty())
    }

    @Test
    fun `game state isHardMode can be set`() {
        val state = GameState(isHardMode = true)
        assertTrue(state.isHardMode)
    }

    @Test
    fun `game state hiddenTileIds can be set`() {
        val state = GameState(hiddenTileIds = setOf(1, 2, 3))
        assertEquals(setOf(1, 2, 3), state.hiddenTileIds)
    }

    @Test
    fun `hiddenTileIds can be cleared via copy`() {
        val state = GameState(hiddenTileIds = setOf(5, 6))
        val cleared = state.copy(hiddenTileIds = emptySet())
        assertTrue(cleared.hiddenTileIds.isEmpty())
    }

    @Test
    fun `TierAnnouncement enum includes INSANE_MODE`() {
        assertTrue(
            "INSANE_MODE should be in TierAnnouncement enum",
            TierAnnouncement.entries.contains(TierAnnouncement.INSANE_MODE)
        )
    }

    @Test
    fun `TierAnnouncement enum has expected count`() {
        assertEquals(7, TierAnnouncement.entries.size)
    }
}

class GameActionTest {

    @Test
    fun `action type enum has all expected values`() {
        val types = ActionType.entries
        assertEquals(15, types.size)
        assertTrue(types.contains(ActionType.TAP_CORRECT))
        assertTrue(types.contains(ActionType.TAP_WRONG))
        assertTrue(types.contains(ActionType.GAME_START))
        assertTrue(types.contains(ActionType.GAME_OVER))
        assertTrue(types.contains(ActionType.GRID_TRANSITION))
        assertTrue(types.contains(ActionType.TUTORIAL_START))
        assertTrue(types.contains(ActionType.TUTORIAL_STEP))
        assertTrue(types.contains(ActionType.TUTORIAL_COMPLETE))
        assertTrue(types.contains(ActionType.PAUSE))
        assertTrue(types.contains(ActionType.RESUME))
        assertTrue(types.contains(ActionType.REVIVE))
        assertTrue(types.contains(ActionType.TIER_ANNOUNCEMENT))
        assertTrue(types.contains(ActionType.SHARE))
        assertTrue(types.contains(ActionType.SCORE_MILESTONE))
        assertTrue(types.contains(ActionType.ERROR))
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

    @Test
    fun `game action full construction`() {
        val action = GameAction(
            timestamp = 2000L,
            type = ActionType.TAP_WRONG,
            tileRow = 2,
            tileCol = 3,
            tileValue = 7,
            targetValue = 5,
            score = 10,
            timeRemaining = 12.5
        )
        assertEquals(2000L, action.timestamp)
        assertEquals(ActionType.TAP_WRONG, action.type)
        assertEquals(2, action.tileRow)
        assertEquals(3, action.tileCol)
        assertEquals(7, action.tileValue)
        assertEquals(5, action.targetValue)
        assertEquals(10, action.score)
        assertEquals(12.5, action.timeRemaining, 0.01)
    }
}

class DifficultyTierTest {

    @Test
    fun `difficulty tier construction`() {
        val tier = DifficultyTier(6, 6, 0.3, 4.0)
        assertEquals(6, tier.gridRows)
        assertEquals(6, tier.gridCols)
        assertEquals(0.3, tier.timeGainSeconds, 0.01)
        assertEquals(4.0, tier.timePenaltySeconds, 0.01)
    }

    @Test
    fun `difficulty tier copy`() {
        val tier = DifficultyTier(4, 4, 1.0, 1.5)
        val modified = tier.copy(gridRows = 5, gridCols = 5)
        assertEquals(5, modified.gridRows)
        assertEquals(5, modified.gridCols)
        assertEquals(4, tier.gridRows) // original unchanged
    }

    // --- #194: isChaosMode tests ---

    @Test
    fun `difficulty tier isChaosMode defaults to false`() {
        val tier = DifficultyTier(4, 4, 1.0, 1.5)
        assertFalse(tier.isChaosMode)
    }

    @Test
    fun `difficulty tier isChaosMode can be set`() {
        val tier = DifficultyTier(6, 6, 0.5, 3.0, "INSANE", isChaosMode = true)
        assertTrue(tier.isChaosMode)
    }

    @Test
    fun `only INSANE tier has isChaosMode true in defaults`() {
        DifficultyConfig.resetDefaults()
        val chaosTiers = DifficultyConfig.tiers.filter { it.isChaosMode }
        assertEquals("Exactly one tier should have isChaosMode=true", 1, chaosTiers.size)
        assertEquals("INSANE", chaosTiers[0].label)
    }
}

class TileStateTest {

    @Test
    fun `tile state enum values`() {
        val states = TileState.entries
        assertEquals(3, states.size)
        assertEquals(TileState.ACTIVE, states[0])
        assertEquals(TileState.TAPPED_CORRECT, states[1])
        assertEquals(TileState.TAPPED_WRONG, states[2])
    }

    @Test
    fun `tile state valueOf`() {
        assertEquals(TileState.ACTIVE, TileState.valueOf("ACTIVE"))
        assertEquals(TileState.TAPPED_CORRECT, TileState.valueOf("TAPPED_CORRECT"))
        assertEquals(TileState.TAPPED_WRONG, TileState.valueOf("TAPPED_WRONG"))
    }
}
