package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.FloatingText
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.TierAnnouncement
import com.xarlord.numbertap.data.TileState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameStateComputedPropertiesTest {

    @Test
    fun `accuracy returns 0 when totalTaps is 0`() {
        val state = GameState(totalTaps = 0, correctTaps = 0)
        assertEquals(0f, state.accuracy, 0.001f)
    }

    @Test
    fun `accuracy returns 1 when all taps correct`() {
        val state = GameState(totalTaps = 10, correctTaps = 10)
        assertEquals(1.0f, state.accuracy, 0.001f)
    }

    @Test
    fun `accuracy returns correct ratio for mixed taps`() {
        val state = GameState(totalTaps = 10, correctTaps = 7)
        assertEquals(0.7f, state.accuracy, 0.001f)
    }

    @Test
    fun `accuracy returns correct ratio for half correct`() {
        val state = GameState(totalTaps = 4, correctTaps = 2)
        assertEquals(0.5f, state.accuracy, 0.001f)
    }

    @Test
    fun `accuracy with one wrong tap`() {
        val state = GameState(totalTaps = 3, correctTaps = 2)
        assertEquals(2f / 3f, state.accuracy, 0.001f)
    }

    @Test
    fun `avgTapTimeMs returns 0 when correctTaps is 0`() {
        val state = GameState(correctTaps = 0)
        assertEquals(0.0, state.avgTapTimeMs, 0.01)
    }

    @Test
    fun `avgTapTimeMs returns 0 when correctTaps is 1`() {
        val state = GameState(correctTaps = 1, totalTapTimeNs = 1_000_000)
        assertEquals(0.0, state.avgTapTimeMs, 0.01)
    }

    @Test
    fun `avgTapTimeMs converts nanoseconds to milliseconds`() {
        // 1 second between taps = 1_000_000_000 ns, with 2 correct taps
        val state = GameState(correctTaps = 3, totalTapTimeNs = 2_000_000_000L)
        // avg = 2_000_000_000 ns / (3-1) = 1_000_000_000 ns = 1000 ms
        assertEquals(1000.0, state.avgTapTimeMs, 0.01)
    }

    @Test
    fun `avgTapTimeMs with small time between taps`() {
        // 200ms between taps
        val state = GameState(correctTaps = 5, totalTapTimeNs = 800_000_000L)
        // avg = 800_000_000 / (5-1) = 200_000_000 ns = 200 ms
        assertEquals(200.0, state.avgTapTimeMs, 0.01)
    }
}

class FloatingTextTest {

    @Test
    fun `floatingText construction`() {
        val ft = FloatingText(
            id = 1,
            text = "+1.0s",
            x = 2.0f,
            y = 3.0f,
            colorHex = 0xFF22C55E,
            createdAt = 1000L
        )
        assertEquals(1, ft.id)
        assertEquals("+1.0s", ft.text)
        assertEquals(2.0f, ft.x, 0.01f)
        assertEquals(3.0f, ft.y, 0.01f)
        assertEquals(0xFF22C55E, ft.colorHex)
        assertEquals(1000L, ft.createdAt)
    }

    @Test
    fun `floatingText equality`() {
        val ft1 = FloatingText(id = 1, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 0L)
        val ft2 = FloatingText(id = 1, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 0L)
        assertEquals(ft1, ft2)
    }

    @Test
    fun `floatingText copy`() {
        val ft = FloatingText(id = 1, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 0L)
        val copy = ft.copy(text = "+0.5s")
        assertEquals("+0.5s", copy.text)
        assertEquals(1, copy.id)
    }

    @Test
    fun `gameState starts with empty floatingTexts`() {
        val state = GameState()
        assertTrue(state.floatingTexts.isEmpty())
    }

    @Test
    fun `gameState can accumulate floatingTexts`() {
        val ft1 = FloatingText(id = 0, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 1000L)
        val ft2 = FloatingText(id = 1, text = "+0.7s", x = 1f, y = 2f, colorHex = 0L, createdAt = 2000L)
        val state = GameState(floatingTexts = listOf(ft1, ft2))
        assertEquals(2, state.floatingTexts.size)
        assertEquals("+1.0s", state.floatingTexts[0].text)
        assertEquals("+0.7s", state.floatingTexts[1].text)
    }
}

class GameEnginePauseResumeReviveTest {

    private lateinit var engine: GameEngine
    private lateinit var playingState: GameState

    @Before
    fun setup() {
        engine = GameEngine()
        playingState = engine.startNewGame(0)
    }

    // --- Pause tests ---

    @Test
    fun `pause sets isPaused to true`() {
        val paused = engine.pause(playingState)
        assertTrue(paused.isPaused)
    }

    @Test
    fun `pause does not change isPlaying`() {
        val paused = engine.pause(playingState)
        assertTrue(paused.isPlaying)
    }

    @Test
    fun `pause does not change score`() {
        val paused = engine.pause(playingState)
        assertEquals(0, paused.score)
    }

    @Test
    fun `pause does not change timeRemaining`() {
        val paused = engine.pause(playingState)
        assertEquals(30.0, paused.timeRemaining, 0.01)
    }

    @Test
    fun `pause when not playing returns unchanged state`() {
        val idle = GameState()
        val result = engine.pause(idle)
        assertEquals(idle, result)
    }

    @Test
    fun `pause when game over returns unchanged state`() {
        val gameOver = GameState(isPlaying = false, isGameOver = true)
        val result = engine.pause(gameOver)
        assertEquals(gameOver, result)
    }

    // --- Resume tests ---

    @Test
    fun `resume clears isPaused`() {
        val paused = engine.pause(playingState)
        val resumed = engine.resume(paused)
        assertFalse(resumed.isPaused)
    }

    @Test
    fun `resume keeps isPlaying true`() {
        val paused = engine.pause(playingState)
        val resumed = engine.resume(paused)
        assertTrue(resumed.isPlaying)
    }

    @Test
    fun `resume when not paused returns unchanged state`() {
        val result = engine.resume(playingState)
        assertEquals(playingState, result)
    }

    @Test
    fun `resume does not change score or time`() {
        val state = playingState.copy(score = 10, timeRemaining = 15.0)
        val paused = engine.pause(state)
        val resumed = engine.resume(paused)
        assertEquals(10, resumed.score)
        assertEquals(15.0, resumed.timeRemaining, 0.01)
    }

    // --- Revive tests ---

    @Test
    fun `revive sets time to 5 seconds`() {
        val gameOver = GameState(isPlaying = false, isGameOver = true, score = 10, highScore = 20)
        val revived = engine.revive(gameOver)
        assertEquals(5.0, revived.timeRemaining, 0.01)
    }

    @Test
    fun `revive sets isPlaying true`() {
        val gameOver = GameState(isPlaying = false, isGameOver = true, score = 10, highScore = 20)
        val revived = engine.revive(gameOver)
        assertTrue(revived.isPlaying)
    }

    @Test
    fun `revive sets isGameOver false`() {
        val gameOver = GameState(isPlaying = false, isGameOver = true, score = 10, highScore = 20)
        val revived = engine.revive(gameOver)
        assertFalse(revived.isGameOver)
    }

    @Test
    fun `revive sets isPaused false`() {
        val gameOver = GameState(isPlaying = false, isGameOver = true, score = 10, highScore = 20)
        val revived = engine.revive(gameOver)
        assertFalse(revived.isPaused)
    }

    @Test
    fun `revive when still playing returns unchanged state`() {
        val result = engine.revive(playingState)
        assertEquals(playingState, result)
    }

    @Test
    fun `revive when not game over returns unchanged state`() {
        val state = GameState(isPlaying = false, isGameOver = false)
        val result = engine.revive(state)
        assertEquals(state, result)
    }

    @Test
    fun `revive preserves score`() {
        val gameOver = GameState(isPlaying = false, isGameOver = true, score = 42, highScore = 50)
        val revived = engine.revive(gameOver)
        assertEquals(42, revived.score)
    }

    // --- isReviveEligible tests ---

    @Test
    fun `isReviveEligible when score at 90 percent of highScore`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 18, highScore = 20)
        assertTrue(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible when score above 90 percent`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 19, highScore = 20)
        assertTrue(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible when score equals highScore`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 20, highScore = 20)
        assertTrue(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible false when score below 90 percent`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 10, highScore = 20)
        assertFalse(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible false when not game over`() {
        val state = GameState(isPlaying = true, isGameOver = false, score = 20, highScore = 20)
        assertFalse(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible false when highScore is zero`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 10, highScore = 0)
        assertFalse(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible at exact 90 percent boundary`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 9, highScore = 10)
        assertTrue(engine.isReviveEligible(state))
    }

    @Test
    fun `isReviveEligible just below 90 percent`() {
        val state = GameState(isPlaying = false, isGameOver = true, score = 8, highScore = 10)
        assertFalse(engine.isReviveEligible(state))
    }
}

class GameEngineTutorialTest {

    private val engine = GameEngine()

    @Test
    fun `startTutorial creates 3x3 grid`() {
        val state = engine.startTutorial(0)
        assertEquals(3, state.tiles.size)
        assertEquals(3, state.tiles[0].size)
        assertEquals(9, state.tiles.flatten().size)
    }

    @Test
    fun `startTutorial sets timeRemaining to 999`() {
        val state = engine.startTutorial(0)
        assertEquals(999.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `startTutorial sets isTutorial true`() {
        val state = engine.startTutorial(0)
        assertTrue(state.isTutorial)
    }

    @Test
    fun `startTutorial sets tutorialStep to 0`() {
        val state = engine.startTutorial(0)
        assertEquals(0, state.tutorialStep)
    }

    @Test
    fun `startTutorial sets gridSize to 3`() {
        val state = engine.startTutorial(0)
        assertEquals(3, state.gridSize)
    }

    @Test
    fun `tutorial grid contains values 1 through 5`() {
        val state = engine.startTutorial(0)
        val values = state.tiles.flatten().map { it.currentValue }.sorted()
        assertTrue("Should contain 1", values.contains(1))
        assertTrue("Should contain 2", values.contains(2))
        assertTrue("Should contain 3", values.contains(3))
        assertTrue("Should contain 4", values.contains(4))
        assertTrue("Should contain 5", values.contains(5))
    }

    @Test
    fun `tutorial grid contains 9 tiles total`() {
        val state = engine.startTutorial(0)
        assertEquals(9, state.tiles.flatten().size)
    }

    @Test
    fun `tutorial correct tap does not add time`() {
        val state = engine.startTutorial(0)
        val time = state.timeRemaining
        val (row, col) = findTileWithValue(state, 1)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(time, newState.timeRemaining, 0.01)
    }

    @Test
    fun `tutorial wrong tap does not deduct time`() {
        val state = engine.startTutorial(0)
        val time = state.timeRemaining
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(time, newState.timeRemaining, 0.01)
    }

    @Test
    fun `tutorial wrong tap does not shake`() {
        val state = engine.startTutorial(0)
        val (row, col) = findTileWithValue(state, 2)!!
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(Pair(0f, 0f), newState.shakeOffset)
    }

    @Test
    fun `tutorial completion at score 5 transitions to real game`() {
        var state = engine.startTutorial(0)
        val time = System.currentTimeMillis()

        for (i in 1..5) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }

        assertFalse("Should no longer be tutorial", state.isTutorial)
        assertEquals(30.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `tutorial completion keeps 3x3 grid since no regeneration on master`() {
        var state = engine.startTutorial(0)
        val time = System.currentTimeMillis()

        for (i in 1..5) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }

        // Master does not regenerate grid on tutorial completion
        assertEquals(3, state.gridSize)
        assertFalse(state.isTutorial)
        assertEquals(30.0, state.timeRemaining, 0.01)
    }

    @Test
    fun `tutorial preserves highScore`() {
        val state = engine.startTutorial(42)
        assertEquals(42, state.highScore)
    }

    @Test
    fun `startNewGame with isTutorial true matches startTutorial`() {
        val direct = engine.startTutorial(10)
        val viaStart = engine.startNewGame(10, isTutorial = true)
        assertEquals(direct.isTutorial, viaStart.isTutorial)
        assertEquals(direct.gridSize, viaStart.gridSize)
        assertEquals(direct.timeRemaining, viaStart.timeRemaining, 0.01)
    }

    @Test
    fun `tutorial before completion stays in tutorial mode`() {
        var state = engine.startTutorial(0)
        val time = System.currentTimeMillis()

        // Tap only 3 (not 5)
        for (i in 1..3) {
            val pos = findTileWithValue(state, i)!!
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }

        assertTrue(state.isTutorial)
        assertEquals(3, state.gridSize)
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

class GameEngineClearExpiredFloatingTextsTest {

    private val engine = GameEngine()

    @Test
    fun `clearExpiredFloatingTexts removes old texts`() {
        val ft = FloatingText(id = 0, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 1000L)
        val state = GameState(floatingTexts = listOf(ft), isPlaying = true)
        // At time 2000, the text is 1000ms old — over 800ms threshold
        val cleared = engine.clearExpiredFloatingTexts(state, 2000L)
        assertTrue(cleared.floatingTexts.isEmpty())
    }

    @Test
    fun `clearExpiredFloatingTexts keeps recent texts`() {
        val ft = FloatingText(id = 0, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 1000L)
        val state = GameState(floatingTexts = listOf(ft), isPlaying = true)
        // At time 1500, the text is 500ms old — under 800ms threshold
        val cleared = engine.clearExpiredFloatingTexts(state, 1500L)
        assertEquals(1, cleared.floatingTexts.size)
    }

    @Test
    fun `clearExpiredFloatingTexts at exact boundary keeps text`() {
        val ft = FloatingText(id = 0, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 1000L)
        val state = GameState(floatingTexts = listOf(ft), isPlaying = true)
        // At time 1799, the text is 799ms old — just under 800ms
        val cleared = engine.clearExpiredFloatingTexts(state, 1799L)
        assertEquals(1, cleared.floatingTexts.size)
    }

    @Test
    fun `clearExpiredFloatingTexts at just past boundary removes text`() {
        val ft = FloatingText(id = 0, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 1000L)
        val state = GameState(floatingTexts = listOf(ft), isPlaying = true)
        // At time 1801, the text is 801ms old — just over 800ms
        val cleared = engine.clearExpiredFloatingTexts(state, 1801L)
        assertTrue(cleared.floatingTexts.isEmpty())
    }

    @Test
    fun `clearExpiredFloatingTexts handles multiple texts`() {
        val ft1 = FloatingText(id = 0, text = "+1.0s", x = 0f, y = 0f, colorHex = 0L, createdAt = 1000L)
        val ft2 = FloatingText(id = 1, text = "+0.7s", x = 1f, y = 1f, colorHex = 0L, createdAt = 1500L)
        val state = GameState(floatingTexts = listOf(ft1, ft2), isPlaying = true)
        // At time 1900: ft1 is 900ms old (expired), ft2 is 400ms old (kept)
        val cleared = engine.clearExpiredFloatingTexts(state, 1900L)
        assertEquals(1, cleared.floatingTexts.size)
        assertEquals(1, cleared.floatingTexts[0].id)
    }
}

class GameEngineThemeTest {

    private val engine = GameEngine()

    @Test
    fun `startNewGame preserves TERMINAL theme`() {
        val state = engine.startNewGame(0, currentTheme = GameTheme.TERMINAL)
        assertEquals(GameTheme.TERMINAL, state.currentTheme)
    }

    @Test
    fun `startNewGame preserves CHALKBOARD theme`() {
        val state = engine.startNewGame(0, currentTheme = GameTheme.CHALKBOARD)
        assertEquals(GameTheme.CHALKBOARD, state.currentTheme)
    }

    @Test
    fun `startNewGame preserves MATRIX theme`() {
        val state = engine.startNewGame(0, currentTheme = GameTheme.MATRIX)
        assertEquals(GameTheme.MATRIX, state.currentTheme)
    }

    @Test
    fun `startNewGame default theme is DEFAULT`() {
        val state = engine.startNewGame(0)
        assertEquals(GameTheme.DEFAULT, state.currentTheme)
    }

    @Test
    fun `theme persists through tap`() {
        val state = engine.startNewGame(0, currentTheme = GameTheme.TERMINAL)
        val (row, col) = state.tiles.flatMapIndexed { r, rowTiles ->
            rowTiles.mapIndexed { c, tile -> Pair(r, c) to tile }
        }.firstOrNull { (_, tile) -> tile.currentValue == 1 }?.first ?: return
        val (newState, _) = engine.onTap(state, row, col)
        assertEquals(GameTheme.TERMINAL, newState.currentTheme)
    }

    @Test
    fun `theme persists through tick`() {
        val state = engine.startNewGame(0, currentTheme = GameTheme.MATRIX)
        val ticked = engine.tick(state, 1.0)
        assertEquals(GameTheme.MATRIX, ticked.currentTheme)
    }
}

class GameEngineTierAnnouncementTest {

    private val engine = GameEngine()

    @Test
    fun `clearTierAnnouncement clears announcement`() {
        val state = GameState(tierAnnouncement = TierAnnouncement.NICE, isPlaying = true)
        val cleared = engine.clearTierAnnouncement(state)
        assertNull(cleared.tierAnnouncement)
    }

    @Test
    fun `clearTierAnnouncement with null announcement is no-op`() {
        val state = GameState(tierAnnouncement = null, isPlaying = true)
        val cleared = engine.clearTierAnnouncement(state)
        assertNull(cleared.tierAnnouncement)
    }

    @Test
    fun `score 5 triggers NICE announcement`() {
        val state = playToScore(5)
        assertEquals(TierAnnouncement.NICE, state.tierAnnouncement)
    }

    @Test
    fun `score 10 triggers GREAT announcement`() {
        val state = playToScore(10)
        assertEquals(TierAnnouncement.GREAT, state.tierAnnouncement)
    }

    @Test
    fun `score 16 triggers ROUND_2 announcement`() {
        val state = playToScore(16)
        assertEquals(TierAnnouncement.ROUND_2, state.tierAnnouncement)
    }

    @Test
    fun `score 25 triggers AMAZING announcement`() {
        val state = playToScore(25)
        assertEquals(TierAnnouncement.AMAZING, state.tierAnnouncement)
    }

    @Test
    fun `score 41 triggers HARD_MODE announcement`() {
        val state = playToScore(41)
        assertEquals(TierAnnouncement.HARD_MODE, state.tierAnnouncement)
    }

    @Test
    fun `score 50 triggers LEGENDARY announcement`() {
        val state = playToScore(50)
        assertEquals(TierAnnouncement.LEGENDARY, state.tierAnnouncement)
    }

    @Test
    fun `non-milestone score has no announcement`() {
        val state = playToScore(3)
        assertNull(state.tierAnnouncement)
    }

    @Test
    fun `score 1 has no announcement`() {
        val state = playToScore(1)
        assertNull(state.tierAnnouncement)
    }

    private fun playToScore(targetScore: Int): GameState {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()
        for (i in 1..targetScore) {
            val pos = findTile(state, i) ?: return state
            val (newState, _) = engine.onTap(state, pos.first, pos.second, time + i * 100)
            state = newState
        }
        return state
    }

    private fun findTile(state: GameState, value: Int): Pair<Int, Int>? {
        state.tiles.forEachIndexed { row, rowTiles ->
            rowTiles.forEachIndexed { col, tile ->
                if (tile.currentValue == value) return Pair(row, col)
            }
        }
        return null
    }
}

class GameEngineIsNewHighScoreTest {

    private val engine = GameEngine()

    @Test
    fun `isNewHighScore is false at game start`() {
        val state = engine.startNewGame(0)
        assertFalse(state.isNewHighScore)
    }

    @Test
    fun `isNewHighScore is true when score exceeds highScore`() {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()
        val pos = findTile(state, 1)!!
        val (newState, _) = engine.onTap(state, pos.first, pos.second, time)
        assertTrue(newState.isNewHighScore)
        assertEquals(1, newState.highScore)
    }

    @Test
    fun `isNewHighScore is false when score does not exceed highScore`() {
        var state = engine.startNewGame(10)
        val time = System.currentTimeMillis()
        val pos = findTile(state, 1)!!
        val (newState, _) = engine.onTap(state, pos.first, pos.second, time)
        assertFalse(newState.isNewHighScore)
        assertEquals(10, newState.highScore)
    }

    @Test
    fun `isNewHighScore updates when score catches up to highScore`() {
        var state = engine.startNewGame(0)
        val time = System.currentTimeMillis()
        // First tap sets highScore to 1, isNewHighScore=true
        val pos1 = findTile(state, 1)!!
        val (s1, _) = engine.onTap(state, pos1.first, pos1.second, time + 100)
        assertTrue(s1.isNewHighScore)

        // Second tap: score=2, highScore=1, so isNewHighScore=true
        val pos2 = findTile(s1, 2)!!
        val (s2, _) = engine.onTap(s1, pos2.first, pos2.second, time + 200)
        assertTrue(s2.isNewHighScore)
        assertEquals(2, s2.highScore)
    }

    private fun findTile(state: GameState, value: Int): Pair<Int, Int>? {
        state.tiles.forEachIndexed { row, rowTiles ->
            rowTiles.forEachIndexed { col, tile ->
                if (tile.currentValue == value) return Pair(row, col)
            }
        }
        return null
    }
}
