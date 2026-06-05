package com.xarlord.numbertap.data

data class GameState(
    val tiles: List<List<Tile>> = emptyList(),
    val targetNumber: Int = 1,
    val score: Int = 0,
    val timeRemaining: Double = 30.0,
    val highScore: Int = 0,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val gridSize: Int = 4,
    val shakeOffset: Pair<Float, Float> = Pair(0f, 0f),
    val comboCount: Int = 0,
    val lastCorrectTapTime: Long = 0L,
    val isTutorial: Boolean = false,
    val tutorialStep: Int = 0,
    val tierAnnouncement: String? = null,
    val floatingTexts: List<FloatingText> = emptyList(),
    val isNewHighScore: Boolean = false,
    val nextFloatingTextId: Int = 0
)

data class FloatingText(
    val id: Int,
    val text: String,
    val x: Float,
    val y: Float,
    val colorHex: Long,
    val createdAt: Long
)
