package com.xarlord.numbertap.data

/**
 * Configurable difficulty system — all parameters tunable without code changes.
 * Issue #100: Difficulty curve is now fully configurable.
 */
data class DifficultyTier(
    val gridRows: Int,
    val gridCols: Int,
    val timeGainSeconds: Double,
    val timePenaltySeconds: Double,
    val label: String = "",
    val scoreThreshold: Int = 0,
    val isChaosMode: Boolean = false  // #194: type-safe chaos mode flag
) {
    val gridSize: Int get() = gridRows * gridCols
}

object DifficultyConfig {

    /** Default tiers per GDD §5.1 — can be overridden for tuning */
    var tiers: List<DifficultyTier> = listOf(
        DifficultyTier(4, 4, 1.0, 1.5, "EASY", scoreThreshold = 0),
        DifficultyTier(4, 4, 0.7, 2.0, "MEDIUM", scoreThreshold = 16, isChaosMode = true),
        DifficultyTier(5, 5, 0.5, 3.0, "HARD", scoreThreshold = 41, isChaosMode = true),
        DifficultyTier(5, 5, 0.4, 3.5, "INSANE", scoreThreshold = 66, isChaosMode = true)
    )

    // #239: Removed unused fields (startingTime, tickIntervalMs, reviveBonusSeconds,
    // reviveEligibilityThreshold, comboWindowMs) — GameConfig is the single source of truth.

    fun tierForScore(score: Int): DifficultyTier {
        return tiers.last { score >= it.scoreThreshold }
    }

    fun currentTierIndex(score: Int): Int {
        return tiers.indexOfLast { score >= it.scoreThreshold }
    }

    /** Reset to GDD defaults */
    fun resetDefaults() {
        tiers = listOf(
            DifficultyTier(4, 4, 1.0, 1.5, "EASY", scoreThreshold = 0),
            DifficultyTier(4, 4, 0.7, 2.0, "MEDIUM", scoreThreshold = 16, isChaosMode = true),
            DifficultyTier(5, 5, 0.5, 3.0, "HARD", scoreThreshold = 41, isChaosMode = true),
            DifficultyTier(5, 5, 0.4, 3.5, "INSANE", scoreThreshold = 66, isChaosMode = true)
        )
    }
}
