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

    @Test
    fun `startNewGame all tiles are ACTIVE`() {
        state.tiles.flatten().forEach { tile ->
            assertEquals(TileState.ACTIVE, tile.state)
        }
    }

    @Test
    fun `startNewGame combo starts at 0`() {
        assertEquals(0, state.comboCount)
    }

    @Test
    fun `startNewGame shake offset is zero`() {
        assertEquals(Pair(0f, 0f), state.shakeOffset)
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
    fun `correct tap adds time for easy tier`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(31.0, newState.timeRemaining, 0.01)
    }

    @Test
    fun `correct tap replaces tile with currentValue + gridSize per GDD`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        val tappedTile = newState.tiles[row][col]
        assertEquals(17, tappedTile.currentValue) // 1 + 16 = 17
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
        val (s2, result2) = engine.onTap(s1, r2, c2, time + 200)
        assertEquals(2, (result2 as TapResult.Correct).combo)
        assertEquals(2, s2.comboCount)
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

    @Test
    fun `correct tap updates highScore when score exceeds it`() {
        val startState = engine.startNewGame(5)
        val (row, col) = findTileWithValue(startState, 1)!!
        val (newState, _) = engine.onTap(startState, row, col)
        assertEquals(1, newState.score)
        assertEquals(5, newState.highScore) // highScore = max(old, new)
    }

    @Test
    fun `correct tap updates highScore when new score exceeds old`() {
        val startState = engine.startNewGame(0)
        val (row, col) = findTileWithValue(startState, 1)!!
        val (newState, _) = engine.onTap(startState, row, col)
        assertEquals(1, newState.highScore)
    }

    @Test
    fun `multiple correct taps create sequential replacement values`() {
        var currentState = state
        val time = 1000L
        val replacements = mutableListOf<Int>()

        for (i in 1..5) {
            val pos = findTileWithValue(currentState, i)
            assertNotNull("Should find value $i", pos)
            val (newState, _) = engine.onTap(currentState, pos!!.first, pos.second, time + i * 100)
            replacements.add(newState.tiles[pos.first][pos.second].currentValue)
            currentState = newState
        }

        // Each replacement = original + 16
        assertEquals(listOf(17, 18, 19, 20, 21), replacements)
    }

    @Test
    fun `correct tap advances target sequentially`() {
        var currentState = state
        val time = 1000L

        for (expectedTarget in 1..10) {
            assertEquals(expectedTarget, currentState.targetNumber)
            val pos = findTileWithValue(currentState, expectedTarget)!!
            val (newState, _) = engine.onTap(currentState, pos.first, pos.second, time + expectedTarget * 100)
            currentState = newState
        }
        assertEquals(11, currentState.targetNumber)
    }

    // --- onTap wrong tests ---

    @Test
    fun `wrong tap returns Wrong result`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (_, result) = engine.onTap(state, row, col)
        assertTrue(result is TapResult.Wrong)
    }

    @Test
    fun `wrong tap deducts time - easy tier`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(28.5, newState.timeRemaining, 0.01) // 30 - 1.5
    }

    @Test
    fun `wrong tap sets shake offset`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertNotEquals(Pair(0f, 0f), newState.shakeOffset)
    }

    @Test
    fun `wrong tap shake offset is within plus minus 6`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        val (dx, dy) = newState.shakeOffset
        assertTrue("dx=$dx should be >= -6", dx >= -6f)
        assertTrue("dx=$dx should be <= 6", dx <= 6f)
        assertTrue("dy=$dy should be >= -6", dy >= -6f)
        assertTrue("dy=$dy should be <= 6", dy <= 6f)
    }

    @Test
    fun `wrong tap resets combo`() {
        val time = 1000L
        val (r1, c1) = findTileWithValue(state, 1)!!
        val (s1, _) = engine.onTap(state, r1, c1, time)
        // Tap wrong number
        val (r2, c2) = findTileWithValue(s1, s1.targetNumber + 1)!!
        val (newState, result) = engine.onTap(s1, r2, c2, time + 100)
        assertEquals(0, newState.comboCount)
        assertTrue("Wrong result should carry previousCombo", (result as TapResult.Wrong).previousCombo == 1)
    }

    @Test
    fun `wrong tap sets TAPPED_WRONG state on tile`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(TileState.TAPPED_WRONG, newState.tiles[row][col].state)
    }

    @Test
    fun `wrong tap does not change target`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(1, newState.targetNumber)
    }

    @Test
    fun `wrong tap does not change score`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(0, newState.score)
    }

    @Test
    fun `wrong tap does not change tile value`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(2, newState.tiles[row][col].currentValue)
    }

    @Test
    fun `time cannot go below zero from wrong taps`() {
        val lowTime = GameState(timeRemaining = 0.5, isPlaying = true, tiles = state.tiles)
        val (row, col) = findTileWithValue(lowTime, 2)!!
        val (newState, _) = engine.onTap(lowTime, row, col)
        assertTrue("Time should not go negative", newState.timeRemaining >= 0.0)
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
    fun `resetTileStates clears TAPPED_CORRECT`() {
        val (row, col) = findTileWithValue(state, 1)!!
        val (s1, _) = engine.onTap(state, row, col)
        assertEquals(TileState.TAPPED_CORRECT, s1.tiles[row][col].state)
        val reset = engine.resetTileStates(s1)
        assertEquals(TileState.ACTIVE, reset.tiles[row][col].state)
    }

    @Test
    fun `resetTileStates clears TAPPED_WRONG`() {
        val (row, col) = findTileWithValue(state, 2)!!
        val (s1, _) = engine.onTap(state, row, col)
        assertEquals(TileState.TAPPED_WRONG, s1.tiles[row][col].state)
        val reset = engine.resetTileStates(s1)
        assertEquals(TileState.ACTIVE, reset.tiles[row][col].state)
    }

    @Test
    fun `resetTileStates does not change ACTIVE tiles`() {
        val count = state.tiles.flatten().count { it.state == TileState.ACTIVE }
        val reset = engine.resetTileStates(state)
        val resetCount = reset.tiles.flatten().count { it.state == TileState.ACTIVE }
        assertEquals(count, resetCount)
    }

    // --- tick tests ---

    @Test
    fun `tick reduces time by delta`() {
        val newState = engine.tick(state, 1.0)
        assertEquals(29.0, newState.timeRemaining, 0.01)
    }

    @Test
    fun `tick reduces time by fractional delta`() {
        val newState = engine.tick(state, 0.016)
        assertEquals(29.984, newState.timeRemaining, 0.001)
    }

    @Test
    fun `tick causes game over when time reaches zero`() {
        val newState = engine.tick(state, 31.0)
        assertFalse(newState.isPlaying)
        assertTrue(newState.isGameOver)
        assertEquals(0.0, newState.timeRemaining, 0.01)
    }

    @Test
    fun `tick causes game over at exactly zero`() {
        val newState = engine.tick(state, 30.0)
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

    @Test
    fun `multiple ticks accumulate correctly`() {
        var s = state
        for (i in 1..10) {
            s = engine.tick(s, 1.0)
        }
        assertEquals(20.0, s.timeRemaining, 0.01)
        assertTrue(s.isPlaying)
    }

    @Test
    fun `tick preserves score and target`() {
        val s = state.copy(score = 5, targetNumber = 3)
        val ticked = engine.tick(s, 1.0)
        assertEquals(5, ticked.score)
        assertEquals(3, ticked.targetNumber)
    }

    // --- invalid tap tests ---

    @Test
    fun `tap out of bounds negative row returns Invalid`() {
        val (_, result) = engine.onTap(state, -1, 0)
        assertTrue(result is TapResult.Invalid)
    }

    @Test
    fun `tap out of bounds negative col returns Invalid`() {
        val (_, result) = engine.onTap(state, 0, -1)
        assertTrue(result is TapResult.Invalid)
    }

    @Test
    fun `tap out of bounds over row returns Invalid`() {
        val (_, result) = engine.onTap(state, 99, 0)
        assertTrue(result is TapResult.Invalid)
    }

    @Test
    fun `tap when not playing returns Invalid`() {
        val idle = GameState()
        val (_, result) = engine.onTap(idle, 0, 0)
        assertTrue(result is TapResult.Invalid)
    }

    @Test
    fun `invalid tap does not change state`() {
        val (newState, _) = engine.onTap(state, -1, 0)
        assertEquals(state, newState)
    }

    // --- grid transition test ---

    @Test
    fun `grid expands from 4x4 to 5x5 at score 41`() {
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

    @Test
    fun `grid transition generates correct value range`() {
        var currentState = state
        val time = System.currentTimeMillis()
        for (i in 1..41) {
            val pos = findTileWithValue(currentState, i)
            if (pos != null) {
                val (newState, _) = engine.onTap(currentState, pos.first, pos.second, time + i * 100)
                currentState = newState
            }
        }
        // Grid should contain values starting from target (42) to target+24 (66)
        val values = currentState.tiles.flatten().map { it.currentValue }.sorted()
        assertEquals(42, values.first())
        assertEquals(66, values.last())
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

    @Test
    fun `shuffle contains all values 1 to N`() {
        for (trial in 1..5) {
            val s = engine.startNewGame(0)
            val values = s.tiles.flatten().map { it.currentValue }.sorted()
            assertEquals((1..16).toList(), values)
        }
    }

    // --- immutability test ---

    @Test
    fun `onTap does not mutate original state`() {
        val originalTarget = state.targetNumber
        val originalScore = state.score
        val originalTime = state.timeRemaining
        val (row, col) = findTileWithValue(state, 1)!!
        engine.onTap(state, row, col)
        assertEquals(originalTarget, state.targetNumber)
        assertEquals(originalScore, state.score)
        assertEquals(originalTime, state.timeRemaining, 0.01)
    }

    @Test
    fun `tick does not mutate original state`() {
        val originalTime = state.timeRemaining
        engine.tick(state, 5.0)
        assertEquals(originalTime, state.timeRemaining, 0.01)
    }

    // --- TapResult sealed class tests ---

    @Test
    fun `TapResult Correct holds combo value`() {
        val result = TapResult.Correct(5)
        assertEquals(5, result.combo)
    }

    @Test
    fun `TapResult Wrong holds previousCombo`() {
        val r1 = TapResult.Wrong(previousCombo = 3)
        val r2 = TapResult.Wrong(previousCombo = 3)
        assertEquals(r1, r2)
        assertEquals(3, r1.previousCombo)
    }

    @Test
    fun `TapResult Wrong with different combos are not equal`() {
        val r1 = TapResult.Wrong(previousCombo = 3)
        val r2 = TapResult.Wrong(previousCombo = 0)
        assertNotEquals(r1, r2)
    }

    @Test
    fun `TapResult Invalid is singleton`() {
        val r1 = TapResult.Invalid
        val r2 = TapResult.Invalid
        assertEquals(r1, r2)
    }

    // --- combo pitch test with wrong tap interruption ---

    @Test
    fun `combo builds then resets on wrong tap`() {
        val time = 1000L
        var currentState = state

        // Build combo: tap 1, 2, 3 correctly
        for (i in 1..3) {
            val pos = findTileWithValue(currentState, i)!!
            val (newState, _) = engine.onTap(currentState, pos.first, pos.second, time + i * 100)
            currentState = newState
        }
        assertEquals(3, currentState.comboCount)

        // Wrong tap resets combo
        val wrongPos = findTileWithValue(currentState, currentState.targetNumber + 1)!!
        val (afterWrong, _) = engine.onTap(currentState, wrongPos.first, wrongPos.second, time + 400)
        assertEquals(0, afterWrong.comboCount)
    }

    // --- difficulty progression integration ---

    @Test
    fun `medium tier reduces time gain`() {
        var currentState = state
        val time = System.currentTimeMillis()
        // Get to score 16 (medium tier)
        for (i in 1..16) {
            val pos = findTileWithValue(currentState, i)
            if (pos != null) {
                val (newState, _) = engine.onTap(currentState, pos.first, pos.second, time + i * 100)
                currentState = newState
            }
        }
        assertEquals(16, currentState.score)
        // The 16th tap used medium tier config (0.7s gain)
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
