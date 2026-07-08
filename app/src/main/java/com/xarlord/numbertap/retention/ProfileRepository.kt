package com.xarlord.numbertap.retention

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Persists PlayerProfile via SharedPreferences.
 * Issue #89: Cross-session retention data.
 */
class ProfileRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("number_tap_profile", Context.MODE_PRIVATE)

    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    fun loadProfile(): PlayerProfile {
        val coins = prefs.getInt("coins", 0)
        val streak = prefs.getInt("current_streak", 0)
        val lastLogin = prefs.getString("last_login_date", "") ?: ""
        val bestStreak = prefs.getInt("best_streak", 0)
        val totalGames = prefs.getInt("total_games", 0)
        val totalTaps = prefs.getLong("total_correct_taps", 0L)
        val highScore = prefs.getInt("profile_high_score", 0)
        val notifEnabled = prefs.getBoolean("notification_enabled", true)
        val missionDate = prefs.getString("mission_date", "") ?: ""

        val missionsJson = prefs.getString("missions", "[]") ?: "[]"
        val missions = parseMissions(missionsJson)

        val powerUpsJson = prefs.getString("power_ups", "{}") ?: "{}"
        val powerUps = parsePowerUps(powerUpsJson)

        return PlayerProfile(
            coins = coins,
            currentStreak = streak,
            lastLoginDate = lastLogin,
            bestStreak = bestStreak,
            totalGamesPlayed = totalGames,
            totalCorrectTaps = totalTaps,
            highScore = highScore,
            todayMissions = missions,
            missionDate = missionDate,
            ownedPowerUps = powerUps,
            notificationEnabled = notifEnabled
        )
    }

    fun saveProfile(profile: PlayerProfile) {
        prefs.edit().apply {
            putInt("coins", profile.coins)
            putInt("current_streak", profile.currentStreak)
            putString("last_login_date", profile.lastLoginDate)
            putInt("best_streak", profile.bestStreak)
            putInt("total_games", profile.totalGamesPlayed)
            putLong("total_correct_taps", profile.totalCorrectTaps)
            putInt("profile_high_score", profile.highScore)
            putString("missions", serializeMissions(profile.todayMissions))
            putString("mission_date", profile.missionDate)
            putString("power_ups", serializePowerUps(profile.ownedPowerUps))
            putBoolean("notification_enabled", profile.notificationEnabled)
            apply()
        }
    }

    /**
     * Process daily login: update streak, award coins, generate missions.
     * Returns updated profile.
     */
    fun processDailyLogin(profile: PlayerProfile): PlayerProfile {
        val today = LocalDate.now().format(dateFormat)
        if (profile.lastLoginDate == today) return profile // Already logged in today

        val yesterday = LocalDate.now().minusDays(1).format(dateFormat)
        val newStreak = if (profile.lastLoginDate == yesterday) {
            profile.currentStreak + 1
        } else {
            1 // Streak broken or first time
        }

        val coinsAwarded = StreakRewards.coinsForDay(newStreak)
        val bonusPowerUp = StreakRewards.powerUpBonusForDay(newStreak)

        val updatedPowerUps = profile.ownedPowerUps.toMutableMap()
        bonusPowerUp?.let {
            updatedPowerUps[it] = (updatedPowerUps[it] ?: 0) + 1
        }

        val newMissions = if (profile.missionDate != today) {
            MissionGenerator.generate(today)
        } else {
            profile.todayMissions
        }

        return profile.copy(
            coins = profile.coins + coinsAwarded,
            currentStreak = newStreak,
            bestStreak = maxOf(profile.bestStreak, newStreak),
            lastLoginDate = today,
            todayMissions = newMissions,
            missionDate = today,
            ownedPowerUps = updatedPowerUps
        )
    }

    /**
     * Purchase a revive (extra time after game over).
     * Returns null if insufficient coins.
     */
    fun purchaseRevive(profile: PlayerProfile, cost: Int): PlayerProfile? {
        if (profile.coins < cost) return null
        return profile.copy(coins = profile.coins - cost)
    }

    /**
     * Use a power-up. Returns null if none owned.
     */
    fun usePowerUp(profile: PlayerProfile, type: PowerUpType): PlayerProfile? {
        val count = profile.ownedPowerUps[type] ?: 0
        if (count <= 0) return null
        val updated = profile.ownedPowerUps.toMutableMap()
        if (count == 1) updated.remove(type) else updated[type] = count - 1
        return profile.copy(ownedPowerUps = updated)
    }

    private fun parseMissions(json: String): List<DailyMission> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            DailyMission(
                id = obj.getString("id"),
                type = MissionType.valueOf(obj.getString("type")),
                target = obj.getInt("target"),
                progress = obj.optInt("progress", 0),
                coinReward = obj.getInt("coinReward"),
                isCompleted = obj.optBoolean("completed", false),
                isClaimed = obj.optBoolean("claimed", false)
            )
        }
    }

    private fun serializeMissions(missions: List<DailyMission>): String {
        val arr = JSONArray()
        missions.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("type", m.type.name)
                put("target", m.target)
                put("progress", m.progress)
                put("coinReward", m.coinReward)
                put("completed", m.isCompleted)
                put("claimed", m.isClaimed)
            })
        }
        return arr.toString()
    }

    private fun parsePowerUps(json: String): Map<PowerUpType, Int> {
        val obj = JSONObject(json)
        return PowerUpType.entries.associateWith { type ->
            obj.optInt(type.name, 0)
        }.filterValues { it > 0 }
    }

    private fun serializePowerUps(powerUps: Map<PowerUpType, Int>): String {
        val obj = JSONObject()
        powerUps.forEach { (type, count) -> obj.put(type.name, count) }
        return obj.toString()
    }
}
