package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.DifficultyConfig
import com.xarlord.numbertap.data.GameConfig
import com.xarlord.numbertap.data.GameState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IssueFixTest {

    private lateinit var engine: GameEngine

    @Before
    fun setup() {
        engine = GameEngine()
        DifficultyConfig.resetDefaults()
    }

    // ============================================================
    // #204: Flip mechanic not working after pass 20
    // ============================================================

    @Test
    fun `#204 - MEDIUM tier (score 16+) has chaos mode enabled`() {
        val mediumTier = DifficultyConfig.tierForScore(20)
        assertTrue("MEDIUM tier should have chaos mode (flip)", mediumTier.isChaosMode)
    }

    @Test
    fun `#204 - EASY tier (score 0-15) does NOT have chaos mode`() {
        val easyTier = DifficultyConfig.tierForScore(0)
        assertFalse("EASY tier should NOT have chaos mode", easyTier.isChaosMode)
    }

    @Test
    fun `#204 - HARD tier (score 41+) has chaos mode enabled`() {
        val hardTier = DifficultyConfig.tierForScore(50)
        assertTrue("HARD tier should have chaos mode (flip)", hardTier.isChaosMode)
    }

    @Test
    fun `#204 - INSANE tier (score 66+) has chaos mode enabled`() {
        val insaneTier = DifficultyConfig.tierForScore(100)
        assertTrue("INSANE tier should have chaos mode", insaneTier.isChaosMode)
    }

    @Test
    fun `#204 - chaosTick hides tiles at score 20 (MEDIUM tier)`() {
        val state = engine.startNewGame(0)
        var currentState = state.copy(score = 20)
        val chaosState = engine.chaosTick(currentState)
        // chaosTick should either hide tiles or reveal them (toggles)
        // Since initial hiddenTileIds is empty, it should hide tiles
        assertTrue("chaosTick should hide tiles at score 20", chaosState.hiddenTileIds.isNotEmpty())
    }

    // ============================================================
    // #205: Combo text positioning (purely UI, tested in Compose)
    // ============================================================

    // Combo text is a UI concern - verified via instrumented tests.
    // The key property is that gameState.comboCount is correct.

    @Test
    fun `#205 - comboCount increments correctly for UI positioning`() {
        var state = engine.startNewGame(0)
        // Rapid taps should produce combo > 1
        val now = System.currentTimeMillis()
        val firstResult = engine.onTap(state, findTargetRow(state), findTargetCol(state), now)
        state = firstResult.first
        val secondResult = engine.onTap(state, findTargetRow(state), findTargetCol(state), now + 100)
        state = secondResult.first
        assertTrue("Combo should be > 1 after rapid correct taps", state.comboCount > 1)
    }

    // ============================================================
    // #206: Coin cost for revive
    // ============================================================

    @Test
    fun `#206 - COIN_COST_FOR_REVIVE is positive`() {
        assertTrue("Coin cost should be positive", GameConfig.COIN_COST_FOR_REVIVE > 0)
    }

    @Test
    fun `#206 - COIN_COST_FOR_REVIVE is 50`() {
        assertEquals(50, GameConfig.COIN_COST_FOR_REVIVE)
    }

    @Test
    fun `#206 - REVIVE_BONUS_SECONDS is 5`() {
        assertEquals(5.0, GameConfig.REVIVE_BONUS_SECONDS, 0.01)
    }

    // ============================================================
    // #207: Flip animation - animatingTileId
    // ============================================================

    @Test
    fun `#207 - correct tap sets animatingTileId`() {
        var state = engine.startNewGame(0)
        val now = System.currentTimeMillis()
        val targetRow = findTargetRow(state)
        val targetCol = findTargetCol(state)
        val (newState, result) = engine.onTap(state, targetRow, targetCol, now)

        assertNotNull("Animating tile ID should be set after correct tap", newState.animatingTileId)
        assertEquals("Animating tile ID should match tapped tile",
            state.tiles[targetRow][targetCol].id, newState.animatingTileId)
    }

    @Test
    fun `#207 - clearAnimatingTile clears the flag`() {
        var state = engine.startNewGame(0).copy(animatingTileId = 5)
        val cleared = engine.clearAnimatingTile(state)
        assertNull("animatingTileId should be null after clear", cleared.animatingTileId)
    }

    @Test
    fun `#207 - TILE_FLIP_DURATION_MS is reasonable`() {
        assertTrue("Flip duration should be between 100-500ms",
            GameConfig.TILE_FLIP_DURATION_MS in 100..500)
    }

    @Test
    fun `#207 - animatingTileId defaults to null`() {
        val state = engine.startNewGame(0)
        assertNull("animatingTileId should default to null", state.animatingTileId)
    }

    @Test
    fun `#207 - wrong tap does NOT set animatingTileId`() {
        var state = engine.startNewGame(0)
        val now = System.currentTimeMillis()
        // Find a non-target tile to tap
        val wrongTile = state.tiles.flatten().first { it.currentValue != state.targetNumber }
        val wrongRow = state.tiles.indexOfFirst { row -> row.any { it.id == wrongTile.id } }
        val wrongCol = state.tiles[wrongRow].indexOfFirst { it.id == wrongTile.id }
        val (newState, result) = engine.onTap(state, wrongRow, wrongCol, now)

        assertNull("animatingTileId should NOT be set on wrong tap", newState.animatingTileId)
    }

    // ============================================================
    // Helper: find the row/col of the target number
    // ============================================================

    private fun findTargetRow(state: GameState): Int {
        return state.tiles.indexOfFirst { row -> row.any { it.currentValue == state.targetNumber } }
    }

    private fun findTargetCol(state: GameState): Int {
        val row = findTargetRow(state)
        if (row < 0) return -1
        return state.tiles[row].indexOfFirst { it.currentValue == state.targetNumber }
    }
}
