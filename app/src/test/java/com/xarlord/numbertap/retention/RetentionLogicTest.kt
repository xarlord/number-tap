package com.xarlord.numbertap.retention

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for retention system — pure logic, zero Android deps.
 * Issue #89: Retention system. Issue #94: Notification support.
 */
class RetentionLogicTest {

    // === StreakRewards ===

    @Test
    fun `streak day 1 awards 10 coins`() = assertEquals(10, StreakRewards.coinsForDay(1))

    @Test
    fun `streak day 7 awards 100 coins`() = assertEquals(100, StreakRewards.coinsForDay(7))

    @Test
    fun `streak day 8 cycles back to day 1`() = assertEquals(10, StreakRewards.coinsForDay(8))

    @Test
    fun `streak day 14 awards 100 coins`() = assertEquals(100, StreakRewards.coinsForDay(14))

    @Test
    fun `streak day 15 cycles to day 1`() = assertEquals(10, StreakRewards.coinsForDay(15))

    @Test
    fun `powerUp bonus only on day 7 multiples`() {
        assertNull(StreakRewards.powerUpBonusForDay(1))
        assertNull(StreakRewards.powerUpBonusForDay(6))
        assertNotNull(StreakRewards.powerUpBonusForDay(7))
        assertNull(StreakRewards.powerUpBonusForDay(8))
        assertNotNull(StreakRewards.powerUpBonusForDay(14))
    }

    @Test
    fun `streak rewards increase monotonically within week`() {
        val rewards = (1..7).map { StreakRewards.coinsForDay(it) }
        for (i in 1 until rewards.size) {
            assertTrue("Day ${i + 1} >= day $i", rewards[i] >= rewards[i - 1])
        }
    }

    // === PowerUpType ===

    @Test
    fun `powerUp base costs are positive`() {
        PowerUpType.entries.forEach { pu ->
            assertTrue("${pu.name} cost > 0", pu.baseCost > 0)
        }
    }

    @Test
    fun `powerUp tier multiplier works`() {
        assertEquals(30, PowerUpType.HIGHLIGHT.costForTier(1f))
        assertEquals(45, PowerUpType.HIGHLIGHT.costForTier(1.5f))
        assertEquals(60, PowerUpType.HIGHLIGHT.costForTier(2f))
    }

    @Test
    fun `powerUp unlock scores ascending`() {
        val unlocks = PowerUpType.entries.map { it.unlockScore }
        for (i in 1 until unlocks.size) assertTrue(unlocks[i] > unlocks[i - 1])
    }

    @Test
    fun `all powerUps have non-blank names and descriptions`() {
        PowerUpType.entries.forEach { pu ->
            assertTrue(pu.displayName.isNotBlank())
            assertTrue(pu.description.isNotBlank())
        }
    }

    // === DailyMission ===

    @Test
    fun `mission progress 0 at start`() {
        val m = DailyMission("t", MissionType.SCORE_TARGET, 10, 0, 20)
        assertEquals(0f, m.progressPercent, 0.01f)
    }

    @Test
    fun `mission progress 1 when complete`() {
        val m = DailyMission("t", MissionType.SCORE_TARGET, 10, 10, 20, isCompleted = true)
        assertEquals(1f, m.progressPercent, 0.01f)
    }

    @Test
    fun `mission progress 0_5 at halfway`() {
        val m = DailyMission("t", MissionType.SCORE_TARGET, 10, 5, 20)
        assertEquals(0.5f, m.progressPercent, 0.01f)
    }

    @Test
    fun `mission progress caps at 1`() {
        val m = DailyMission("t", MissionType.SCORE_TARGET, 10, 20, 20)
        assertEquals(1f, m.progressPercent, 0.01f)
    }

    // === MissionGenerator ===

    @Test
    fun `generator produces 3 missions`() = assertEquals(3, MissionGenerator.generate("2026-01-15").size)

    @Test
    fun `generator produces unique IDs`() {
        val missions = MissionGenerator.generate("2026-01-15")
        assertEquals(3, missions.map { it.id }.toSet().size)
    }

    @Test
    fun `generator same date same missions`() {
        val a = MissionGenerator.generate("2026-03-20")
        val b = MissionGenerator.generate("2026-03-20")
        assertEquals(a.map { it.type }, b.map { it.type })
        assertEquals(a.map { it.target }, b.map { it.target })
    }

    @Test
    fun `generator missions have positive targets and rewards`() {
        MissionGenerator.generate("2026-05-10").forEach { m ->
            assertTrue(m.target > 0)
            assertTrue(m.coinReward > 0)
        }
    }

    @Test
    fun `generator missions start fresh`() {
        MissionGenerator.generate("2026-05-10").forEach { m ->
            assertFalse(m.isCompleted)
            assertFalse(m.isClaimed)
            assertEquals(0, m.progress)
        }
    }

    // === RetentionLogic — coins & combos ===

    @Test
    fun `awardTapCoins adds coins`() = assertEquals(15, RetentionLogic.awardTapCoins(10, 5))

    @Test
    fun `awardComboBonus combo 3 gives 1`() = assertEquals(1, RetentionLogic.awardComboBonus(0, 3))

    @Test
    fun `awardComboBonus combo 5 gives 3`() = assertEquals(3, RetentionLogic.awardComboBonus(0, 5))

    @Test
    fun `awardComboBonus combo 10 gives 5`() = assertEquals(5, RetentionLogic.awardComboBonus(0, 10))

    @Test
    fun `awardComboBonus combo 1 gives 0`() = assertEquals(0, RetentionLogic.awardComboBonus(0, 1))

    // === RetentionLogic — purchase & use ===

    @Test
    fun `purchasePowerUp deducts coins`() {
        val (coins, ups) = RetentionLogic.purchasePowerUp(100, emptyMap(), PowerUpType.HIGHLIGHT)!!
        assertEquals(70, coins)
        assertEquals(1, ups[PowerUpType.HIGHLIGHT])
    }

    @Test
    fun `purchasePowerUp returns null if insufficient`() {
        assertNull(RetentionLogic.purchasePowerUp(10, emptyMap(), PowerUpType.HIGHLIGHT))
    }

    @Test
    fun `usePowerUp decrements count`() {
        val result = RetentionLogic.usePowerUp(mapOf(PowerUpType.EXTRA_TIME to 3), PowerUpType.EXTRA_TIME)!!
        assertEquals(2, result[PowerUpType.EXTRA_TIME])
    }

    @Test
    fun `usePowerUp removes at zero`() {
        val result = RetentionLogic.usePowerUp(mapOf(PowerUpType.EXTRA_TIME to 1), PowerUpType.EXTRA_TIME)!!
        assertFalse(result.containsKey(PowerUpType.EXTRA_TIME))
    }

    @Test
    fun `usePowerUp returns null if none owned`() {
        assertNull(RetentionLogic.usePowerUp(emptyMap(), PowerUpType.EXTRA_TIME))
    }

    // === RetentionLogic — missions ===

    @Test
    fun `updateMissionProgress marks score mission complete`() {
        val missions = listOf(DailyMission("m1", MissionType.SCORE_TARGET, 20, 0, 30))
        val result = RetentionLogic.updateMissionProgress(missions, gameScore = 25)
        assertTrue(result[0].isCompleted)
        assertEquals(25, result[0].progress)
    }

    @Test
    fun `updateMissionProgress does not regress`() {
        val missions = listOf(DailyMission("m1", MissionType.SCORE_TARGET, 50, 30, 30))
        val result = RetentionLogic.updateMissionProgress(missions, gameScore = 10)
        assertEquals(30, result[0].progress)
    }

    @Test
    fun `updateMissionProgress increments games played`() {
        val missions = listOf(DailyMission("m1", MissionType.GAMES_PLAYED, 5, 2, 30))
        val result = RetentionLogic.updateMissionProgress(missions, gamesPlayed = 1)
        assertEquals(3, result[0].progress)
        assertFalse(result[0].isCompleted)
    }

    @Test
    fun `updateMissionProgress games played reaches target`() {
        val missions = listOf(DailyMission("m1", MissionType.GAMES_PLAYED, 3, 2, 30))
        val result = RetentionLogic.updateMissionProgress(missions, gamesPlayed = 1)
        assertTrue(result[0].isCompleted)
    }

    @Test
    fun `claimMission awards coins and marks claimed`() {
        val missions = listOf(DailyMission("m1", MissionType.SCORE_TARGET, 10, 10, 25, isCompleted = true))
        val (coins, updated) = RetentionLogic.claimMission(0, missions, "m1")
        assertEquals(25, coins)
        assertTrue(updated[0].isClaimed)
    }

    @Test
    fun `claimMission no coins for incomplete`() {
        val missions = listOf(DailyMission("m1", MissionType.SCORE_TARGET, 10, 5, 25))
        val (coins, _) = RetentionLogic.claimMission(0, missions, "m1")
        assertEquals(0, coins)
    }

    @Test
    fun `claimMission no double claim`() {
        val missions = listOf(DailyMission("m1", MissionType.SCORE_TARGET, 10, 10, 25, isCompleted = true, isClaimed = true))
        val (coins, _) = RetentionLogic.claimMission(0, missions, "m1")
        assertEquals(0, coins)
    }

    // === RetentionLogic — streak ===

    @Test
    fun `streak continues if last login was yesterday`() {
        assertEquals(6, RetentionLogic.calculateStreak(5, "2026-01-14", "2026-01-15", "2026-01-14"))
    }

    @Test
    fun `streak resets if gap is more than 1 day`() {
        assertEquals(1, RetentionLogic.calculateStreak(5, "2026-01-10", "2026-01-15", "2026-01-14"))
    }

    @Test
    fun `streak unchanged if already logged in today`() {
        assertEquals(0, RetentionLogic.calculateStreak(5, "2026-01-15", "2026-01-15", "2026-01-14"))
    }

    @Test
    fun `first ever login starts streak at 1`() {
        assertEquals(1, RetentionLogic.calculateStreak(0, "", "2026-01-15", "2026-01-14"))
    }

    // === PlayerProfile defaults ===

    @Test
    fun `default profile has zeros`() {
        val p = PlayerProfile()
        assertEquals(0, p.coins)
        assertEquals(0, p.currentStreak)
        assertEquals(0, p.bestStreak)
        assertEquals(0, p.totalGamesPlayed)
        assertTrue(p.ownedPowerUps.isEmpty())
        assertTrue(p.todayMissions.isEmpty())
    }

    @Test
    fun `profile copy works`() {
        val p = PlayerProfile(coins = 100, currentStreak = 5)
        val p2 = p.copy(coins = 200)
        assertEquals(200, p2.coins)
        assertEquals(5, p2.currentStreak)
        assertEquals(100, p.coins) // original unchanged
    }
}
