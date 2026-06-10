package com.xarlord.numbertap.retention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerProfileTest {

    // --- StreakRewards ---

    @Test
    fun `coinsForDay returns correct daily amounts`() {
        assertEquals(10, StreakRewards.coinsForDay(1))
        assertEquals(15, StreakRewards.coinsForDay(2))
        assertEquals(20, StreakRewards.coinsForDay(3))
        assertEquals(25, StreakRewards.coinsForDay(4))
        assertEquals(30, StreakRewards.coinsForDay(5))
        assertEquals(50, StreakRewards.coinsForDay(6))
        assertEquals(100, StreakRewards.coinsForDay(7))
    }

    @Test
    fun `coinsForDay cycles after day 7`() {
        assertEquals(StreakRewards.coinsForDay(1), StreakRewards.coinsForDay(8))
        assertEquals(StreakRewards.coinsForDay(2), StreakRewards.coinsForDay(9))
        assertEquals(StreakRewards.coinsForDay(7), StreakRewards.coinsForDay(14))
    }

    @Test
    fun `coinsForDay handles edge case day 0 gracefully`() {
        // (0-1) % 7 = -1, coerceIn(0,6) = 0
        val result = StreakRewards.coinsForDay(0)
        assertEquals(10, result) // index 0
    }

    @Test
    fun `powerUpBonusForDay returns null for non-weekly days`() {
        assertNull(StreakRewards.powerUpBonusForDay(1))
        assertNull(StreakRewards.powerUpBonusForDay(6))
        assertNull(StreakRewards.powerUpBonusForDay(13))
    }

    @Test
    fun `powerUpBonusForDay returns HIGHLIGHT on every 7th day`() {
        assertEquals(PowerUpType.HIGHLIGHT, StreakRewards.powerUpBonusForDay(7))
        assertEquals(PowerUpType.HIGHLIGHT, StreakRewards.powerUpBonusForDay(14))
        assertEquals(PowerUpType.HIGHLIGHT, StreakRewards.powerUpBonusForDay(21))
    }

    // --- DailyMission.progressPercent ---

    @Test
    fun `progressPercent is 0 when no progress`() {
        val mission = DailyMission(id = "test", type = MissionType.SCORE_TARGET, target = 10, coinReward = 20)
        assertEquals(0f, mission.progressPercent, 0.001f)
    }

    @Test
    fun `progressPercent is 1 when completed`() {
        val mission = DailyMission(id = "test", type = MissionType.SCORE_TARGET, target = 10, progress = 10, coinReward = 20)
        assertEquals(1f, mission.progressPercent, 0.001f)
    }

    @Test
    fun `progressPercent clamps above 1`() {
        val mission = DailyMission(id = "test", type = MissionType.SCORE_TARGET, target = 10, progress = 15, coinReward = 20)
        assertEquals(1f, mission.progressPercent, 0.001f)
    }

    @Test
    fun `progressPercent handles zero target`() {
        val mission = DailyMission(id = "test", type = MissionType.SCORE_TARGET, target = 0, coinReward = 20)
        assertEquals(1f, mission.progressPercent, 0.001f)
    }

    @Test
    fun `progressPercent calculates partial progress`() {
        val mission = DailyMission(id = "test", type = MissionType.SCORE_TARGET, target = 20, progress = 5, coinReward = 20)
        assertEquals(0.25f, mission.progressPercent, 0.001f)
    }

    // --- PowerUpType.costForTier ---

    @Test
    fun `costForTier default multiplier is base cost`() {
        val slowMotion = PowerUpType.SLOW_MOTION
        assertEquals(50, slowMotion.costForTier(1f))
    }

    @Test
    fun `costForTier applies tier multiplier`() {
        val slowMotion = PowerUpType.SLOW_MOTION
        assertEquals(75, slowMotion.costForTier(1.5f))
        assertEquals(100, slowMotion.costForTier(2f))
    }

    @Test
    fun `all powerups have positive baseCost and unlockScore`() {
        PowerUpType.entries.forEach { pu ->
            assertTrue("${pu.name} baseCost should be positive", pu.baseCost > 0)
            assertTrue("${pu.name} unlockScore should be positive", pu.unlockScore > 0)
        }
    }
}
