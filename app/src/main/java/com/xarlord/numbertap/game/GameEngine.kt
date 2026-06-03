package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.*
import kotlin.random.Random

class GameEngine {
    private var state = GameState()
    private var nextReplacementValue: Int = 0

    fun getState(): GameState = state

    fun startNewGame(highScore: Int): GameState {
        val tier = DifficultyConfig.tierForScore(0)
        nextReplacementValue = tier.gridRows * tier.gridCols + 1
        val tiles = generateGrid(tier.gridRows, tier.gridCols, tier.maxSpawnedValue)
        state = GameState(
            tiles = tiles,
            targetNumber = 1,
            score = 0,
            timeRemaining = 30.0,
            highScore = highScore,
            isPlaying = true,
            isGameOver = false,
            gridSize = tier.gridRows,
            comboCount = 0,
            lastCorrectTapTime = 0L
        )
        return state
    }

    fun onTap(row: Int, col: Int, currentTime: Long = System.currentTimeMillis()): TapResult {
        if (!state.isPlaying || row < 0 || row >= state.tiles.size || col < 0 || col >= state.tiles[row].size) {
            return TapResult.Invalid
        }
        val tile = state.tiles[row][col]
        val tier = DifficultyConfig.tierForScore(state.score)

        return if (tile.currentValue == state.targetNumber) {
            handleCorrectTap(row, col, tile, tier, currentTime)
        } else {
            handleWrongTap(tier)
        }
    }

    private fun handleCorrectTap(row: Int, col: Int, tile: Tile, tier: DifficultyTier, currentTime: Long): TapResult.Correct {
        val newScore = state.score + 1
        val newTimeRemaining = state.timeRemaining + tier.timeGainSeconds
        val newTarget = state.targetNumber + 1

        // Update tile with replacement value
        val newTiles = state.tiles.map { rowList ->
            rowList.map { t ->
                if (t.id == tile.id) t.copy(currentValue = nextReplacementValue, state = TileState.TAPPED_CORRECT)
                else if (t.state != TileState.ACTIVE) t.copy(state = TileState.ACTIVE)
                else t
            }
        }
        nextReplacementValue++

        // Check for grid transition
        val newTier = DifficultyConfig.tierForScore(newScore)
        val finalTiles = if (newTier.gridRows != tier.gridRows) {
            regenerateForNewGrid(newTier, newTiles, newTarget)
        } else {
            newTiles
        }

        // Combo logic
        val timeSinceLastTap = if (state.lastCorrectTapTime > 0) currentTime - state.lastCorrectTapTime else Long.MAX_VALUE
        val newCombo = if (timeSinceLastTap < 500) state.comboCount + 1 else 1

        state = state.copy(
            tiles = finalTiles,
            targetNumber = newTarget,
            score = newScore,
            timeRemaining = newTimeRemaining,
            highScore = maxOf(state.highScore, newScore),
            gridSize = newTier.gridRows,
            comboCount = newCombo,
            lastCorrectTapTime = currentTime
        )
        return TapResult.Correct(newCombo)
    }

    private fun handleWrongTap(tier: DifficultyTier): TapResult.Wrong {
        val newTime = maxOf(0.0, state.timeRemaining - tier.timePenaltySeconds)
        state = state.copy(
            timeRemaining = newTime,
            comboCount = 0,
            shakeOffset = Pair(
                (Random.nextFloat() * 12 - 6),
                (Random.nextFloat() * 12 - 6)
            )
        )
        return TapResult.Wrong
    }

    fun clearShake() {
        state = state.copy(shakeOffset = Pair(0f, 0f))
    }

    fun tick(deltaSeconds: Double): GameState {
        if (!state.isPlaying) return state
        val newTime = state.timeRemaining - deltaSeconds
        return if (newTime <= 0) {
            state = state.copy(timeRemaining = 0.0, isPlaying = false, isGameOver = true)
            state
        } else {
            state = state.copy(timeRemaining = newTime)
            state
        }
    }

    private fun generateGrid(rows: Int, cols: Int, maxValue: Int): List<List<Tile>> {
        val size = rows * cols
        val values = (1..minOf(maxValue, size)).toMutableList()
        while (values.size < size) values.add(values.size + 1)
        val shuffled = values.toMutableList()
        // Fisher-Yates shuffle
        for (i in shuffled.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            shuffled[i] = shuffled[j].also { shuffled[j] = shuffled[i] }
        }
        var idx = 0
        return (0 until rows).map { r ->
            (0 until cols).map { c ->
                Tile(id = r * cols + c, currentValue = shuffled[idx++])
            }
        }
    }

    private fun regenerateForNewGrid(tier: DifficultyTier, oldTiles: List<List<Tile>>, currentTarget: Int): List<List<Tile>> {
        val size = tier.gridRows * tier.gridCols
        val values = (currentTarget until currentTarget + size).toMutableList()
        val shuffled = values.toMutableList()
        for (i in shuffled.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            shuffled[i] = shuffled[j].also { shuffled[j] = shuffled[i] }
        }
        var idx = 0
        return (0 until tier.gridRows).map { r ->
            (0 until tier.gridCols).map { c ->
                Tile(id = r * tier.gridCols + c, currentValue = shuffled[idx++])
            }
        }
    }
}

sealed class TapResult {
    data class Correct(val combo: Int) : TapResult()
    object Wrong : TapResult()
    object Invalid : TapResult()
}
