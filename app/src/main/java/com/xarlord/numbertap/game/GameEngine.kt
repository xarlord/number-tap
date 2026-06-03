package com.xarlord.numbertap.game

import com.xarlord.numbertap.data.DifficultyConfig
import com.xarlord.numbertap.data.DifficultyTier
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import kotlin.random.Random

/**
 * Pure game logic engine. All methods return new GameState without side effects.
 * Compose-safe: no mutable internal state leaked to UI layer.
 */
class GameEngine {

    fun startNewGame(highScore: Int, gridSize: Int = 4): GameState {
        val tier = DifficultyConfig.tierForScore(0)
        val tiles = generateGrid(tier.gridRows, tier.gridCols, tier.maxSpawnedValue)
        return GameState(
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
    }

    fun onTap(state: GameState, row: Int, col: Int, currentTime: Long = System.currentTimeMillis()): Pair<GameState, TapResult> {
        if (!state.isPlaying || row < 0 || row >= state.tiles.size || col < 0 || col >= state.tiles[row].size) {
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
        val newTimeRemaining = state.timeRemaining + tier.timeGainSeconds
        val newTarget = state.targetNumber + 1

        // GDD: tile replacement value = currentValue + gridSize
        val replacementValue = tile.currentValue + (tier.gridRows * tier.gridCols)

        // Update tile — briefly show TAPPED_CORRECT, then replace value and reset state
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
        val finalTiles = if (newTier.gridRows != tier.gridRows) {
            regenerateForNewGrid(newTier, newTarget)
        } else {
            newTiles
        }

        // Combo logic
        val timeSinceLastTap = if (state.lastCorrectTapTime > 0) currentTime - state.lastCorrectTapTime else Long.MAX_VALUE
        val newCombo = if (timeSinceLastTap < 500) state.comboCount + 1 else 1

        return Pair(state.copy(
            tiles = finalTiles,
            targetNumber = newTarget,
            score = newScore,
            timeRemaining = newTimeRemaining,
            highScore = maxOf(state.highScore, newScore),
            gridSize = newTier.gridRows,
            comboCount = newCombo,
            lastCorrectTapTime = currentTime
        ), TapResult.Correct(newCombo))
    }

    private fun handleWrongTap(state: GameState, row: Int, col: Int, tile: Tile, tier: DifficultyTier): Pair<GameState, TapResult.Wrong> {
        val newTime = maxOf(0.0, state.timeRemaining - tier.timePenaltySeconds)

        // Set TAPPED_WRONG state on the wrong tile for red flash feedback
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
            shakeOffset = Pair(
                Random.nextFloat() * 12 - 6,
                Random.nextFloat() * 12 - 6
            )
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

    fun tick(state: GameState, deltaSeconds: Double): GameState {
        if (!state.isPlaying) return state
        val newTime = state.timeRemaining - deltaSeconds
        return if (newTime <= 0) {
            state.copy(timeRemaining = 0.0, isPlaying = false, isGameOver = true)
        } else {
            state.copy(timeRemaining = newTime)
        }
    }

    private fun generateGrid(rows: Int, cols: Int, maxValue: Int): List<List<Tile>> {
        val size = rows * cols
        val values = (1..minOf(maxValue, size)).toMutableList()
        while (values.size < size) values.add(values.size + 1)
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
    object Wrong : TapResult()
    object Invalid : TapResult()
}
