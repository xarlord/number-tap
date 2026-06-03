package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.TileState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setup() {
        engine = GameEngine()
    }

    // --- startNewGame tests ---

    @Test
    fun `startNewGame creates 4x4 grid`() {
        val state = engine.startNewGame(0)
        assertEquals(4, state.tiles.size)
        assertEquals(4, state.tiles[0].size)
        assertEquals(16, state.tiles.flatten().size)
    }

    @Test
    fun `startNewGame sets target to 1`() {
        val state = engine.startNewGame(0)
        assertEquals(1, state.targetNumber)
    }

    @Test
    fun `startNewGame sets score to 0`() {
        val state = engine.startNewGame(0)
        assertEquals(0, state.score)
    }

    @Test
    fun `startNewGame sets time to 30 seconds`() {
        val state = engine.startNewGame(0)
        assertEquals(30.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `startNewGame sets isPlaying true`() {
        val state = engine.startNewGame(0)
        assertTrue(state.isPlaying)
    }

    @Test
    fun `startNewGame sets isGameOver false`() {
        val state = engine.startNewGame(0)
        assertFalse(state.isGameOver)
    }

    @Test
    fun `startNewGame preserves highScore`() {
        val state = engine.startNewGame(42)
        assertEquals(42, state.highScore)
    }

    @Test
    fun `startNewGame generates shuffled values 1 to 16`() {
        val state = engine.startNewGame(0)
        val values = state.tiles.flatten().map { it.currentValue }.sorted()
        assertEquals((1..16).toList(), values)
    }

    @Test
    fun `startNewGame values are shuffled not sequential`() {
        val state = engine.startNewGame(0)
        val values = state.tiles.flatten().map { it.currentValue }
        // Extremely unlikely to be exactly 1,2,3...16 after shuffle
        assertNotEquals((1..16).toList(), values)
    }

    // --- onTap correct tests ---

    @Test
    fun `correct tap increments target`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 1)!!
        engine.onTap(row, col)
        assertEquals(2, engine.getState().targetNumber)
    }

    @Test
    fun `correct tap increments score`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 1)!!
        engine.onTap(row, col)
        assertEquals(1, engine.getState().score)
    }

    @Test
    fun `correct tap adds time`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 1)!!
        engine.onTap(row, col)
        // 30 + 1.0 = 31.0 for tier 0-15
        assertEquals(31.0, engine.getState().timeRemaining, 0.01)
    }

    @Test
    fun `correct tap replaces tile value`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 1)!!
        engine.onTap(row, col)
        val newState = engine.getState()
        val tappedTile = newState.tiles[row][col]
        // Should be replaced with 17 (16 + 1)
        assertEquals(17, tappedTile.currentValue)
    }

    @Test
    fun `correct tap returns Correct result`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 1)!!
        val result = engine.onTap(row, col)
        assertTrue(result is TapResult.Correct)
        assertEquals(1, (result as TapResult.Correct).combo)
    }

    @Test
    fun `consecutive correct taps build combo`() {
        engine.startNewGame(0)
        val time = 1000L
        var state = engine.getState()
        val (r1, c1) = findTileWithValue(state, 1)!!
        engine.onTap(r1, c1, time)
        state = engine.getState()
        val (r2, c2) = findTileWithValue(state, 2)!!
        val result = engine.onTap(r2, c2, time + 200) // within 500ms
        assertEquals(2, (result as TapResult.Correct).combo)
    }

    @Test
    fun `combo resets after 500ms gap`() {
        engine.startNewGame(0)
        val time = 1000L
        var state = engine.getState()
        val (r1, c1) = findTileWithValue(state, 1)!!
        engine.onTap(r1, c1, time)
        state = engine.getState()
        val (r2, c2) = findTileWithValue(state, 2)!!
        val result = engine.onTap(r2, c2, time + 600) // over 500ms
        assertEquals(1, (result as TapResult.Correct).combo)
    }

    // --- onTap wrong tests ---

    @Test
    fun `wrong tap returns Wrong result`() {
        engine.startNewGame(0)
        val state = engine.getState()
        // Target is 1, tap something that isn't 1
        val (row, col) = findTileWithValue(state, 2)!!
        val result = engine.onTap(row, col)
        assertTrue(result is TapResult.Wrong)
    }

    @Test
    fun `wrong tap deducts time`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 2)!!
        engine.onTap(row, col)
        // 30 - 1.5 = 28.5 for tier 0-15
        assertEquals(28.5, engine.getState().timeRemaining, 0.01)
    }

    @Test
    fun `wrong tap sets shake offset`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 2)!!
        engine.onTap(row, col)
        val offset = engine.getState().shakeOffset
        assertNotEquals(Pair(0f, 0f), offset)
    }

    @Test
    fun `wrong tap resets combo`() {
        engine.startNewGame(0)
        val time = 1000L
        var state = engine.getState()
        val (r1, c1) = findTileWithValue(state, 1)!!
        engine.onTap(r1, c1, time)
        state = engine.getState()
        val (r2, c2) = findTileWithValue(state, state.targetNumber + 1)!! // wrong number
        engine.onTap(r2, c2, time + 100)
        assertEquals(0, engine.getState().comboCount)
    }

    // --- clearShake tests ---

    @Test
    fun `clearShake resets offset to zero`() {
        engine.startNewGame(0)
        val state = engine.getState()
        val (row, col) = findTileWithValue(state, 2)!!
        engine.onTap(row, col)
        engine.clearShake()
        assertEquals(Pair(0f, 0f), engine.getState().shakeOffset)
    }

    // --- tick tests ---

    @Test
    fun `tick reduces time`() {
        engine.startNewGame(0)
        engine.tick(1.0)
        assertEquals(29.0, engine.getState().timeRemaining, 0.01)
    }

    @Test
    fun `tick causes game over when time reaches zero`() {
        engine.startNewGame(0)
        // Tick 30+ seconds
        engine.tick(31.0)
        assertFalse(engine.getState().isPlaying)
        assertTrue(engine.getState().isGameOver)
        assertEquals(0.0, engine.getState().timeRemaining, 0.01)
    }

    @Test
    fun `tick does nothing when not playing`() {
        // Engine not started, default state has isPlaying=false
        val result = engine.tick(1.0)
        // Should return state unchanged (default timeRemaining = 30.0)
        assertEquals(30.0, result.timeRemaining, 0.01)
        assertFalse(result.isPlaying)
    }

    // --- invalid tap tests ---

    @Test
    fun `tap out of bounds returns Invalid`() {
        engine.startNewGame(0)
        val result = engine.onTap(-1, 0)
        assertTrue(result is TapResult.Invalid)
    }

    @Test
    fun `tap when not playing returns Invalid`() {
        // Don't start a game
        val result = engine.onTap(0, 0)
        assertTrue(result is TapResult.Invalid)
    }

    // --- grid transition test ---

    @Test
    fun `grid expands at score 41`() {
        engine.startNewGame(0)
        val time = System.currentTimeMillis()
        // Simulate 41 correct taps
        for (i in 1..41) {
            val state = engine.getState()
            val pos = findTileWithValue(state, i)
            if (pos != null) {
                engine.onTap(pos.first, pos.second, time + i * 100)
            }
        }
        assertEquals(5, engine.getState().gridSize)
        assertEquals(25, engine.getState().tiles.flatten().size)
    }

    // --- Fisher-Yates shuffle test ---

    @Test
    fun `multiple games produce different shuffles`() {
        val state1 = engine.startNewGame(0)
        engine = GameEngine()
        val state2 = engine.startNewGame(0)
        val values1 = state1.tiles.flatten().map { it.currentValue }
        val values2 = state2.tiles.flatten().map { it.currentValue }
        // Extremely unlikely to be identical
        assertNotEquals(values1, values2)
    }

    // --- helper ---

    private fun findTileWithValue(state: com.xarlord.numbertap.data.GameState, value: Int): Pair<Int, Int>? {
        state.tiles.forEachIndexed { row, rowTiles ->
            rowTiles.forEachIndexed { col, tile ->
                if (tile.currentValue == value) return Pair(row, col)
            }
        }
        return null
    }
}
