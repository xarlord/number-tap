package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.TileState
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration tests exercising full game scenarios end-to-end.
 */
class GameFlowIntegrationTest {

    private val engine = GameEngine()

    @Test
    fun `full easy game flow - play 15 correct taps`() {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()

        for (i in 1..15) {
            val pos = findTileWithValue(state, i)!!
            val (newState, result) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            assertTrue("Tap $i should be correct", result is TapResult.Correct)
            state = newState
        }

        assertEquals(15, state.score)
        assertTrue(state.isPlaying)
        assertEquals(16, state.targetNumber) // next target after clearing 15
    }

    @Test
    fun `game over from timer drain`() {
        var state = engine.startNewGame(0)

        // Drain time
        for (i in 1..35) {
            state = engine.tick(state, 1.0)
        }

        assertTrue("Should be game over", state.isGameOver)
        assertFalse(state.isPlaying)
        assertEquals(0.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `time gain offsets timer drain`() {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()

        // Each correct tap adds 1.0s, each tick removes 0.016s
        // With rapid tapping, time should stay relatively stable
        for (i in 1..10) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
            state = engine.tick(state, 0.016)
        }

        // Time should be around 30 + (10 * 1.0) - (10 * 0.016) ≈ 39.84
        assertTrue("Time should be > 30 with correct taps: ${state.timeRemaining}", state.timeRemaining > 30.0)
    }

    @Test
    fun `wrong taps drain time faster than correct taps add`() {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()
        val initialTime = state.timeRemaining

        // 5 correct taps
        for (i in 1..5) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }
        val timeAfterCorrect = state.timeRemaining

        // Now do 5 wrong taps (all on wrong tiles)
        for (i in 1..5) {
            val wrongVal = state.targetNumber + 1
            val pos = findTileWithValue(state, wrongVal)
            if (pos != null) {
                val (newState, _) = engine.onTap(state, pos.first, pos.second, time + (i + 5) * 100)
                state = newState
            }
        }

        // Time should have decreased from wrong taps
        assertTrue("Time after wrong taps (${state.timeRemaining}) should be < time after correct ($timeAfterCorrect)",
            state.timeRemaining < timeAfterCorrect)
    }

    @Test
    fun `high score is tracked across multiple games`() {
        // Game 1: score 5
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()
        for (i in 1..5) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }
        val game1Score = state.score
        val game1HighScore = state.highScore

        // Game 2: start with previous high score
        state = engine.startNewGame(game1HighScore)
        for (i in 1..3) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }

        assertEquals(game1HighScore, state.highScore) // high score persists
    }

    @Test
    fun `shake and feedback lifecycle`() {
        var state = engine.startNewGame(0)

        // Wrong tap triggers shake
        val pos = findTileWithValue(state, 2)!!
        val (afterWrong, result) = engine.onTap(state, pos.first, pos.second)
        assertTrue(result is TapResult.Wrong)
        assertNotEquals(Pair(0f, 0f), afterWrong.shakeOffset)

        // Verify TAPPED_WRONG on the tile
        assertEquals(TileState.TAPPED_WRONG, afterWrong.tiles[pos.first][pos.second].state)

        // Clear feedback
        val cleared = engine.resetTileStates(afterWrong)
        assertEquals(TileState.ACTIVE, cleared.tiles[pos.first][pos.second].state)

        val shakeCleared = engine.clearShake(cleared)
        assertEquals(Pair(0f, 0f), shakeCleared.shakeOffset)
    }

    @Test
    fun `grid values are always unique after each tap`() {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()

        for (i in 1..10) {
            val allValues = state.tiles.flatten().map { it.currentValue }
            val uniqueValues = allValues.toSet()
            assertEquals("All grid values should be unique after $i taps", allValues.size, uniqueValues.size)

            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }
    }

    @Test
    fun `tap result types are exhaustive`() {
        var state = engine.startNewGame(0)

        // Correct tap
        val pos1 = findTileWithValue(state, 1)!!
        val (_, result1) = engine.onTap(state, pos1.first, pos1.second)
        assertTrue("First result should be Correct", result1 is TapResult.Correct)

        // Wrong tap
        val pos2 = findTileWithValue(state, 100) // value that doesn't exist
        if (pos2 == null) {
            val wrongPos = findTileWithValue(state, 3)!!
            val (_, result2) = engine.onTap(state, wrongPos.first, wrongPos.second)
            assertTrue("Wrong tap result should be Wrong", result2 is TapResult.Wrong)
        }

        // Invalid tap
        val (_, result3) = engine.onTap(state, -1, -1)
        assertTrue("Out of bounds result should be Invalid", result3 is TapResult.Invalid)
    }

    private fun findTileWithValue(state: GameState, value: Int): Pair<Int, Int>? {
        state.tiles.forEachIndexed { row, rowTiles ->
            rowTiles.forEachIndexed { col, tile ->
                if (tile.currentValue == value) return Pair(row, col)
            }
        }
        return null
    }
}
