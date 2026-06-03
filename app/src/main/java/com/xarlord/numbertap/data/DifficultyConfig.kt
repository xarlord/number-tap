package com.xarlord.numbertap.data

data class DifficultyTier(
    val gridRows: Int,
    val gridCols: Int,
    val maxSpawnedValue: Int,
    val timeGainSeconds: Double,
    val timePenaltySeconds: Double
)

object DifficultyConfig {
    fun tierForScore(score: Int): DifficultyTier = when {
        score <= 15 -> DifficultyTier(4, 4, 16, 1.0, 1.5)
        score <= 40 -> DifficultyTier(4, 4, 32, 0.7, 2.0)
        else -> DifficultyTier(5, 5, 50, 0.5, 3.0)
    }
}
