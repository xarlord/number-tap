package com.xarlord.numbertap.data

enum class ActionType {
    TAP_CORRECT, TAP_WRONG, GAME_START, GAME_OVER, GRID_TRANSITION
}

data class GameAction(
    val timestamp: Long,
    val type: ActionType,
    val tileRow: Int = -1,
    val tileCol: Int = -1,
    val tileValue: Int = -1,
    val targetValue: Int = -1,
    val score: Int = 0,
    val timeRemaining: Double = 0.0
)
