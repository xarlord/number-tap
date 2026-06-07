package com.xarlord.numbertap.retention

/**
 * Player profile — persisted across sessions.
 * Issue #89: Retention system data model.
 */
data class PlayerProfile(
    val coins: Int = 0,
    val currentStreak: Int = 0,
    val lastLoginDate: String = "",   // yyyy-MM-dd
    val bestStreak: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalCorrectTaps: Long = 0,
    val highScore: Int = 0,
    val todayMissions: List<DailyMission> = emptyList(),
    val missionDate: String = "",     // yyyy-MM-dd (date missions were generated)
    val ownedPowerUps: Map<PowerUpType, Int> = emptyMap(),
    val notificationEnabled: Boolean = true
)

/**
 * Daily mission types.
 */
enum class MissionType {
    SCORE_TARGET,      // Reach score X in a single game
    COMBO_TARGET,      // Reach combo X in a single game
    GAMES_PLAYED,      // Play Y games today
    TOTAL_TAPS         // Accumulate X correct taps today
}

data class DailyMission(
    val id: String,
    val type: MissionType,
    val target: Int,
    val progress: Int = 0,
    val coinReward: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    val progressPercent: Float get() = if (target == 0) 1f else (progress.toFloat() / target).coerceIn(0f, 1f)
}

/**
 * Power-up types with unlock thresholds (score-based).
 */
enum class PowerUpType(
    val displayName: String,
    val unlockScore: Int,
    val baseCost: Int,
    val description: String
) {
    SLOW_MOTION(
        displayName = "Slow Motion",
        unlockScore = 15,
        baseCost = 50,
        description = "Timer runs at half speed for 5 seconds"
    ),
    HIGHLIGHT(
        displayName = "Highlight",
        unlockScore = 30,
        baseCost = 30,
        description = "Target tile glows for 3 seconds"
    ),
    EXTRA_TIME(
        displayName = "Extra Time",
        unlockScore = 50,
        baseCost = 80,
        description = "Add 5 seconds to the clock"
    ),
    PEEK(
        displayName = "Peek",
        unlockScore = 75,
        baseCost = 100,
        description = "Show the next 3 target numbers"
    );

    /** Cost adjusted by difficulty tier (Medium=1.5x, Hard=2x) */
    fun costForTier(tierMultiplier: Float = 1f): Int = (baseCost * tierMultiplier).toInt()
}

/**
 * Streak rewards — Day 1 to Day 7, then cycling.
 */
object StreakRewards {
    private val dailyCoinRewards = intArrayOf(10, 15, 20, 25, 30, 50, 100)

    /** Coins awarded for logging in on streak day N (1-based) */
    fun coinsForDay(streakDay: Int): Int {
        val index = ((streakDay - 1) % 7).coerceIn(0, 6)
        return dailyCoinRewards[index]
    }

    /** Bonus power-up on every 7th day */
    fun powerUpBonusForDay(streakDay: Int): PowerUpType? {
        return if (streakDay % 7 == 0) PowerUpType.HIGHLIGHT else null
    }
}
