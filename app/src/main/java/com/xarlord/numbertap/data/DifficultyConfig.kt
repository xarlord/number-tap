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

    /** Starting countdown time in seconds */
    var startingTime: Double = 30.0

    /** Timer tick interval in milliseconds */
    var tickIntervalMs: Long = 16L

    /** Revive bonus seconds */
    var reviveBonusSeconds: Double = 5.0

    /** Revive eligibility threshold (% of high score) */
    var reviveEligibilityThreshold: Double = 0.9

    /** Combo streak window in milliseconds */
    var comboWindowMs: Long = 500L

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
        startingTime = 30.0
        tickIntervalMs = 16L
        reviveBonusSeconds = 5.0
        reviveEligibilityThreshold = 0.9
        comboWindowMs = 500L
    }
}
