package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.TileState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine
    private lateinit var state: GameState

    @Before
    fun setup() {
        engine = GameEngine()
        state = engine.startNewGame(0)
    }

    // --- startNewGame tests ---

    @Test
    fun `startNewGame creates 4x4 grid`() {
        assertEquals(4, state.tiles.size)
        assertEquals(4, state.tiles[0].size)
        assertEquals(16, state.tiles.flatten().size)
    }

    @Test
    fun `startNewGame sets target to 1`() {
        assertEquals(1, state.targetNumber)
    }

    @Test
    fun `startNewGame sets score to 0`() {
        assertEquals(0, state.score)
    }

    @Test
    fun `startNewGame sets time to 30 seconds`() {
        assertEquals(30.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `startNewGame sets isPlaying true`() {
        assertTrue(state.isPlaying)
    }

    @Test
    fun `startNewGame sets isGameOver false`() {
        assertFalse(state.isGameOver)
    }

    @Test
    fun `startNewGame preserves highScore`() {
        val s = engine.startNewGame(42)
        assertEquals(42, s.highScore)
    }

    @Test
    fun `startNewGame generates shuffled values 1 to 16`() {
        val values = state.tiles.flatten().map { it.currentValue }.sorted()
        assertEquals((1..16).toList(), values)
    }

    @Test
    fun `startNewGame values are shuffled not sequential`() {
        val values = state.tiles.flatten().map { it.currentValue }
        assertNotEquals((1..16).toList(), values)
    }

    // --- onTap correct tests ---

    @Test
    fun `correct tap increments target`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(2, newState.targetNumber)
    }

    @Test
    fun `correct tap increments score`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(1, newState.score)
    }

    @Test
    fun `correct tap adds time`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(31.0, newState.timeRemaining, 0.01)
    }

    @Test
    fun `correct tap replaces tile with currentValue + gridSize`() {
        // GDD: replacement = currentValue + gridSize
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        val tappedTile = newState.tiles[row][col]
        // 1 + 16 = 17
        assertEquals(17, tappedTile.currentValue)
    }

    @Test
    fun `correct tap sets TAPPED_CORRECT state on tile`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(TileState.TAPPED_CORRECT, newState.tiles[row][col].state)
    }

    @Test
    fun `correct tap returns Correct result`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (_, result) = engine.onTap(state, row, col)
        assertTrue(result is TapResult.Correct)
        assertEquals(1, (result as TapResult.Correct).combo)
    }

    @Test
    fun `consecutive correct taps build combo`() {
        val time = 1000L
        val (r1, c1) = findTileWithValue(state, 1)!!
        val (s1, _) = engine.onTap(state, r1, c1, time)
        val (r2, c2) = findTileWithValue(s1, 2)!!
        val (_, result) = engine.onTap(s1, r2, c2, time + 200)
        assertEquals(2, (result as TapResult.Correct).combo)
    }

    @Test
    fun `combo resets after 500ms gap`() {
        val time = 1000L
        val (r1, c1) = findTileWithValue(state, 1)!!
        val (s1, _) = engine.onTap(state, r1, c1, time)
        val (r2, c2) = findTileWithValue(s1, 2)!!
        val (_, result) = engine.onTap(s1, r2, c2, time + 600)
        assertEquals(1, (result as TapResult.Correct).combo)
    }

    // --- onTap wrong tests ---

    @Test
    fun `wrong tap returns Wrong result`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (_, result) = engine.onTap(state, row, col)
        assertTrue(result is TapResult.Wrong)
    }

    @Test
    fun `wrong tap deducts time`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(28.5, newState.timeRemaining, 0.01)
    }

    @Test
    fun `wrong tap sets shake offset`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertNotEquals(Pair(0f, 0f), newState.shakeOffset)
    }

    @Test
    fun `wrong tap resets combo`() {
        val time = 1000L
        val (r1, c1) = findTileWithValue(state, 1)!!
        val (s1, _) = engine.onTap(state, r1, c1, time)
        // Tap wrong number
        val (r2, c2) = findTileWithValue(s1, s1.targetNumber + 1)!!
        val (newState, _) = engine.onTap(s1, r2, c2, time + 100)
        assertEquals(0, newState.comboCount)
    }

    @Test
    fun `wrong tap sets TAPPED_WRONG state on tile`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(TileState.TAPPED_WRONG, newState.tiles[row][col].state)
    }

    // --- clearShake tests ---

    @Test
    fun `clearShake resets offset to zero`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (s1, _) = engine.onTap(state, row, col)
        val cleared = engine.clearShake(s1)
        assertEquals(Pair(0f, 0f), cleared.shakeOffset)
    }

    // --- resetTileStates tests ---

    @Test
    fun `resetTileStates clears all non-ACTIVE states`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (s1, _) = engine.onTap(state, row, col)
        assertEquals(TileState.TAPPED_CORRECT, s1.tiles[row][col].state)
        val reset = engine.resetTileStates(s1)
        assertEquals(TileState.ACTIVE, reset.tiles[row][col].state)
    }

    // --- tick tests ---

    @Test
    fun `tick reduces time`() {
        val newState = engine.tick(state, 1.0)
        assertEquals(29.0, newState.timeRemaining, 0.01)
    }

    @Test
    fun `tick causes game over when time reaches zero`() {
        val newState = engine.tick(state, 31.0)
        assertFalse(newState.isPlaying)
        assertTrue(newState.isGameOver)
        assertEquals(0.0, newState.timeRemaining, 0.01)
    }

    @Test
    fun `tick does nothing when not playing`() {
        val idle = GameState()
        val result = engine.tick(idle, 1.0)
        assertEquals(30.0, result.timeRemaining, 0.01)
        assertFalse(result.isPlaying)
    }

    // --- invalid tap tests ---

    @Test
    fun `tap out of bounds returns Invalid`() {
        val (_, result) = engine.onTap(state, -1, 0)
        assertTrue(result is TapResult.Invalid)
    }

    @Test
    fun `tap when not playing returns Invalid`() {
        val idle = GameState()
        val (_, result) = engine.onTap(idle, 0, 0)
        assertTrue(result is TapResult.Invalid)
    }

    // --- grid transition test ---

    @Test
    fun `grid expands at score 41`() {
        var currentState = state
        val time = System.currentTimeMillis()
        for (i in 1..41) {
            val pos = findTileWithValue(currentState, i)
            if (pos != null) {
                val (newState, _) = engine.onTap(currentState, pos.first, pos.second, time + i * 100)
                currentState = newState
            }
        }
        assertEquals(5, currentState.gridSize)
        assertEquals(25, currentState.tiles.flatten().size)
    }

    // --- Fisher-Yates shuffle test ---

    @Test
    fun `multiple games produce different shuffles`() {
        val state1 = engine.startNewGame(0)
        val state2 = engine.startNewGame(0)
        val values1 = state1.tiles.flatten().map { it.currentValue }
        val values2 = state2.tiles.flatten().map { it.currentValue }
        assertNotEquals(values1, values2)
    }

    // --- immutability test ---

    @Test
    fun `onTap does not mutate original state`() {
        val originalTarget = state.targetNumber
        val originalScore = state.score
        val (row, col) = findTileWithValue(state, 1)!!
        engine.onTap(state, row, col)
        // Original state unchanged
        assertEquals(originalTarget, state.targetNumber)
        assertEquals(originalScore, state.score)
    }

    // --- helper ---

    private fun findTileWithValue(state: GameState, value: Int): Pair<Int, Int>? {
        state.tiles.forEachIndexed { row, rowTiles ->
            rowTiles.forEachIndexed { col, tile ->
                if (tile.currentValue == value) return Pair(row, col)
            }
        }
        return null
    }
}
