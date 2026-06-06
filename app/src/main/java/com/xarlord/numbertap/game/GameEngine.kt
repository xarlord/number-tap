package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.DifficultyConfig
import com.xarlord.numbertap.data.DifficultyTier
import com.xarlord.numbertap.data.FloatingText
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Pure game logic engine. All methods return new GameState without side effects.
 * Compose-safe: no mutable internal state leaked to UI layer.
 */
class GameEngine {

    private val floatingTextCounter = AtomicInteger(0)

    fun startNewGame(highScore: Int, isTutorial: Boolean = false, currentTheme: GameTheme = GameTheme.DEFAULT): GameState {
        val tier = DifficultyConfig.tierForScore(0)
        val tiles = if (isTutorial) {
            generateTutorialGrid()
        } else {
            generateGrid(tier.gridRows, tier.gridCols, tier.maxSpawnedValue)
        }
        return GameState(
            tiles = tiles,
            targetNumber = 1,
            score = 0,
            timeRemaining = if (isTutorial) 999.0 else 30.0,
            highScore = highScore,
            isPlaying = true,
            isGameOver = false,
            isPaused = false,
            gridSize = if (isTutorial) 3 else tier.gridRows,
            comboCount = 0,
            lastCorrectTapTime = 0L,
            isTutorial = isTutorial,
            tutorialStep = if (isTutorial) 0 else -1,
            currentTheme = currentTheme
        )
    }

    fun startTutorial(highScore: Int): GameState {
        return startNewGame(highScore, isTutorial = true)
    }

    fun onTap(state: GameState, row: Int, col: Int, currentTime: Long = System.currentTimeMillis()): Pair<GameState, TapResult> {
        if (!state.isPlaying || state.isPaused) return Pair(state, TapResult.Invalid)
        if (row < 0 || row >= state.tiles.size || col < 0 || col >= state.tiles[row].size) {
            return Pair(state, TapResult.Invalid)
        }
        val tile = state.tiles[row][col]
        val tier = DifficultyConfig.tierForScore(state.score)

        return if (tile.currentValue == state.targetNumber) {
            handleCorrectTap(state, row, col, tile, tier, currentTime)
        } else {
            handleWrongTap(state, row, col, tile, tier)
        }
    }

    private fun handleCorrectTap(state: GameState, row: Int, col: Int, tile: Tile, tier: DifficultyTier, currentTime: Long): Pair<GameState, TapResult.Correct> {
        val newScore = state.score + 1
        val timeGain = if (state.isTutorial) 0.0 else tier.timeGainSeconds
        val newTimeRemaining = state.timeRemaining + timeGain
        val newTarget = state.targetNumber + 1

        // GDD: tile replacement value = currentValue + gridSize
        val gridSizeTotal = if (state.isTutorial) 9 else (tier.gridRows * tier.gridCols)
        val replacementValue = tile.currentValue + gridSizeTotal

        val newTiles = state.tiles.map { rowList ->
            rowList.map { t ->
                if (t.id == tile.id) t.copy(
                    currentValue = replacementValue,
                    state = TileState.TAPPED_CORRECT
                )
                else if (t.state != TileState.ACTIVE) t.copy(state = TileState.ACTIVE)
                else t
            }
        }

        // Check for grid transition
        val newTier = DifficultyConfig.tierForScore(newScore)
        val didTransition = newTier.gridRows != tier.gridRows && !state.isTutorial
        val finalTiles = if (didTransition) {
            regenerateForNewGrid(newTier, newTarget)
        } else {
            newTiles
        }

        // Combo logic
        val timeSinceLastTap = if (state.lastCorrectTapTime > 0) currentTime - state.lastCorrectTapTime else Long.MAX_VALUE
        val newCombo = if (timeSinceLastTap < 500) state.comboCount + 1 else 1

        // Tier announcement
        val tierAnnouncement = when {
            newScore == 16 && !state.isTutorial -> "ROUND 2!"
            newScore == 41 && !state.isTutorial -> "HARD MODE!"
            newScore == 5 && !state.isTutorial -> "NICE!"
            newScore == 10 && !state.isTutorial -> "GREAT!"
            newScore == 25 && !state.isTutorial -> "AMAZING!"
            newScore == 50 && !state.isTutorial -> "LEGENDARY!"
            else -> null
        }

        // Floating text for time gain
        val floatingText = if (!state.isTutorial && timeGain > 0) {
            FloatingText(
                id = floatingTextCounter.getAndIncrement(),
                text = "+${timeGain}s",
                x = col.toFloat(),
                y = row.toFloat(),
                colorHex = 0xFF22C55E,
                createdAt = currentTime
            )
        } else null

        val wasNewHighScore = newScore > state.highScore

        val newState = state.copy(
            tiles = finalTiles,
            targetNumber = newTarget,
            score = newScore,
            timeRemaining = newTimeRemaining,
            highScore = maxOf(state.highScore, newScore),
            gridSize = if (state.isTutorial) 3 else newTier.gridRows,
            comboCount = newCombo,
            maxCombo = maxOf(state.maxCombo, newCombo),
            lastCorrectTapTime = currentTime,
            tierAnnouncement = tierAnnouncement,
            floatingTexts = if (floatingText != null) state.floatingTexts + floatingText else state.floatingTexts,
            isNewHighScore = wasNewHighScore,
            totalTaps = state.totalTaps + 1,
            correctTaps = state.correctTaps + 1,
            totalTapTimeNs = if (state.lastCorrectTapTime > 0) state.totalTapTimeNs + (currentTime - state.lastCorrectTapTime) * 1_000_000 else state.totalTapTimeNs
        )

        // Tutorial step advance
        if (state.isTutorial && newScore >= 5) {
            ActionLogger.logTutorialComplete(newScore)
            return Pair(newState.copy(isTutorial = false, timeRemaining = 30.0), TapResult.Correct(newCombo))
        }

        if (didTransition) {
            ActionLogger.logGridTransition(newScore, newTier.gridRows)
        }

        if (tierAnnouncement != null && newScore % 5 == 0) {
            ActionLogger.logScoreMilestone(newScore, tierAnnouncement)
        }

        return Pair(newState, TapResult.Correct(newCombo))
    }

    private fun handleWrongTap(state: GameState, row: Int, col: Int, tile: Tile, tier: DifficultyTier): Pair<GameState, TapResult.Wrong> {
        val penalty = if (state.isTutorial) 0.0 else tier.timePenaltySeconds
        val newTime = maxOf(0.0, state.timeRemaining - penalty)

        val newTiles = state.tiles.map { rowList ->
            rowList.map { t ->
                if (t.id == tile.id) t.copy(state = TileState.TAPPED_WRONG)
                else if (t.state != TileState.ACTIVE) t.copy(state = TileState.ACTIVE)
                else t
            }
        }

        return Pair(state.copy(
            tiles = newTiles,
            timeRemaining = newTime,
            comboCount = 0,
            shakeOffset = if (state.isTutorial) Pair(0f, 0f) else Pair(
                Random.nextFloat() * 12 - 6,
                Random.nextFloat() * 12 - 6
            ),
            tierAnnouncement = null,
            totalTaps = state.totalTaps + 1,
            wrongTaps = state.wrongTaps + 1
        ), TapResult.Wrong)
    }

    fun clearShake(state: GameState): GameState {
        return state.copy(shakeOffset = Pair(0f, 0f))
    }

    fun resetTileStates(state: GameState): GameState {
        val resetTiles = state.tiles.map { rowList ->
            rowList.map { t ->
                if (t.state != TileState.ACTIVE) t.copy(state = TileState.ACTIVE) else t
            }
        }
        return state.copy(tiles = resetTiles)
    }

    fun clearExpiredFloatingTexts(state: GameState, currentTime: Long): GameState {
        val active = state.floatingTexts.filter { currentTime - it.createdAt < 800 }
        return state.copy(floatingTexts = active)
    }

    fun clearTierAnnouncement(state: GameState): GameState {
        return state.copy(tierAnnouncement = null)
    }

    fun tick(state: GameState, deltaSeconds: Double): GameState {
        if (!state.isPlaying || state.isPaused) return state
        val newTime = state.timeRemaining - deltaSeconds
        return if (newTime <= 0) {
            state.copy(timeRemaining = 0.0, isPlaying = false, isGameOver = true)
        } else {
            state.copy(timeRemaining = newTime)
        }
    }

    fun pause(state: GameState): GameState {
        if (!state.isPlaying || state.isGameOver) return state
        ActionLogger.logPause(state.score, state.timeRemaining)
        return state.copy(isPaused = true)
    }

    fun resume(state: GameState): GameState {
        if (!state.isPaused) return state
        ActionLogger.logResume(state.score, state.timeRemaining)
        return state.copy(isPaused = false)
    }

    fun revive(state: GameState): GameState {
        if (state.isPlaying || !state.isGameOver) return state
        ActionLogger.logRevive(state.score, state.timeRemaining)
        return state.copy(
            timeRemaining = 5.0,
            isPlaying = true,
            isGameOver = false,
            isPaused = false
        )
    }

    fun isReviveEligible(state: GameState): Boolean {
        if (!state.isGameOver || state.highScore == 0) return false
        val threshold = state.highScore * 0.9
        return state.score >= threshold
    }

    private fun generateTutorialGrid(): List<List<Tile>> {
        // 3x3 grid with numbers 1-5 for tutorial (rest filled with higher numbers)
        val values = mutableListOf(1, 2, 3, 4, 5, 10, 15, 20, 25)
        val shuffled = fisherYatesShuffle(values)
        var idx = 0
        return (0 until 3).map { r ->
            (0 until 3).map { c ->
                Tile(id = r * 3 + c, currentValue = shuffled[idx++])
            }
        }
    }

    private fun generateGrid(rows: Int, cols: Int, maxValue: Int): List<List<Tile>> {
        val size = rows * cols
        val values = (1..size).toMutableList()
        val shuffled = fisherYatesShuffle(values)
        var idx = 0
        return (0 until rows).map { r ->
            (0 until cols).map { c ->
                Tile(id = r * cols + c, currentValue = shuffled[idx++])
            }
        }
    }

    private fun regenerateForNewGrid(tier: DifficultyTier, currentTarget: Int): List<List<Tile>> {
        val size = tier.gridRows * tier.gridCols
        val values = (currentTarget until currentTarget + size).toMutableList()
        val shuffled = fisherYatesShuffle(values)
        var idx = 0
        return (0 until tier.gridRows).map { r ->
            (0 until tier.gridCols).map { c ->
                Tile(id = r * tier.gridCols + c, currentValue = shuffled[idx++])
            }
        }
    }

    private fun <T> fisherYatesShuffle(list: MutableList<T>): MutableList<T> {
        for (i in list.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            list[i] = list[j].also { list[j] = list[i] }
        }
        return list
    }
}

sealed class TapResult {
    data class Correct(val combo: Int) : TapResult()
    data object Wrong : TapResult()
    data object Invalid : TapResult()
}
