package com.xarlord.numbertap.data

enum class ActionType {
    TAP_CORRECT, TAP_WRONG, GAME_START, GAME_OVER, GRID_TRANSITION,
    TUTORIAL_START, TUTORIAL_STEP, TUTORIAL_COMPLETE,
    PAUSE, RESUME, REVIVE, TIER_ANNOUNCEMENT, SHARE, SCORE_MILESTONE
}

data class GameAction(
    val timestamp: Long,
    val type: ActionType,
    val tileRow: Int = -1,
    val tileCol: Int = -1,
    val tileValue: Int = -1,
    val targetValue: Int = -1,
    val score: Int = 0,
    val timeRemaining: Double = 0.0,
    val extra: String = ""
)
