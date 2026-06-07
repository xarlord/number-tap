package com.xarlord.numbertap.retention

/**
 * Pure retention logic — no Android dependencies, fully testable.
 * Issue #89: All coin/streak/mission/power-up logic extracted here.
 */
object RetentionLogic {

    /** Award coins for correct taps (1 coin per tap) */
    fun awardTapCoins(coins: Int, tapCount: Int = 1): Int = coins + tapCount

    /** Award combo bonus coins */
    fun awardComboBonus(coins: Int, combo: Int): Int {
        val bonus = when {
            combo >= 10 -> 5
            combo >= 5 -> 3
            combo >= 3 -> 1
            else -> 0
        }
        return coins + bonus
    }

    /** Purchase a power-up. Returns null if insufficient coins. */
    fun purchasePowerUp(
        coins: Int,
        ownedPowerUps: Map<PowerUpType, Int>,
        type: PowerUpType,
        tierMultiplier: Float = 1f
    ): Pair<Int, Map<PowerUpType, Int>>? {
        val cost = type.costForTier(tierMultiplier)
        if (coins < cost) return null
        val updated = ownedPowerUps.toMutableMap()
        updated[type] = (updated[type] ?: 0) + 1
        return Pair(coins - cost, updated.toMap())
    }

    /** Use a power-up. Returns null if none owned. */
    fun usePowerUp(
        ownedPowerUps: Map<PowerUpType, Int>,
        type: PowerUpType
    ): Map<PowerUpType, Int>? {
        val count = ownedPowerUps[type] ?: 0
        if (count <= 0) return null
        val updated = ownedPowerUps.toMutableMap()
        if (count == 1) updated.remove(type) else updated[type] = count - 1
        return updated.toMap()
    }

    /** Update mission progress after a game */
    fun updateMissionProgress(
        missions: List<DailyMission>,
        gameScore: Int = 0,
        maxCombo: Int = 0,
        gamesPlayed: Int = 1,
        correctTaps: Int = 0
    ): List<DailyMission> {
        return missions.map { mission ->
            if (mission.isCompleted) return@map mission
            val newProgress = when (mission.type) {
                MissionType.SCORE_TARGET -> maxOf(mission.progress, gameScore)
                MissionType.COMBO_TARGET -> maxOf(mission.progress, maxCombo)
                MissionType.GAMES_PLAYED -> mission.progress + gamesPlayed
                MissionType.TOTAL_TAPS -> mission.progress + correctTaps
            }
            mission.copy(
                progress = newProgress,
                isCompleted = newProgress >= mission.target
            )
        }
    }

    /** Claim a completed mission. Returns (coins, updatedMissions). */
    fun claimMission(coins: Int, missions: List<DailyMission>, missionId: String): Pair<Int, List<DailyMission>> {
        var awarded = 0
        val updated = missions.map { mission ->
            if (mission.id == missionId && mission.isCompleted && !mission.isClaimed) {
                awarded += mission.coinReward
                mission.copy(isClaimed = true)
            } else mission
        }
        return Pair(coins + awarded, updated)
    }

    /**
     * Calculate streak after daily login check.
     * @param currentStreak Current streak count
     * @param lastLoginDate Last login date (yyyy-MM-dd)
     * @param today Today's date (yyyy-MM-dd)
     * @param yesterday Yesterday's date (yyyy-MM-dd)
     * @return New streak (0 if already logged in today, incremented if yesterday, 1 if new)
     */
    fun calculateStreak(currentStreak: Int, lastLoginDate: String, today: String, yesterday: String): Int {
        if (lastLoginDate == today) return 0 // Already logged in today
        return if (lastLoginDate == yesterday) currentStreak + 1 else 1
    }
}
