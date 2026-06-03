package com.xarlord.numbertap.data

data class GameState(
    val tiles: List<List<Tile>> = emptyList(),
    val targetNumber: Int = 1,
    val score: Int = 0,
    val timeRemaining: Double = 30.0,
    val highScore: Int = 0,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val gridSize: Int = 4,
    val shakeOffset: Pair<Float, Float> = Pair(0f, 0f),
    val comboCount: Int = 0,
    val lastCorrectTapTime: Long = 0L
)
