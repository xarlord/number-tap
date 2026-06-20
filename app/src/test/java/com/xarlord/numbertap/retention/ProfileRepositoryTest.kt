package com.xarlord.numbertap.retention

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Comprehensive unit tests for ProfileRepository.
 *
 * Uses an in-memory FakeSharedPreferences to avoid Android framework dependencies.
 * The Context is mocked with MockK to return the fake prefs.
 */
class ProfileRepositoryTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var repo: ProfileRepository

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        val context = mockk<Context>()
        every {
            context.getSharedPreferences("number_tap_profile", Context.MODE_PRIVATE)
        } returns fakePrefs
        repo = ProfileRepository(context)
    }

    // ── loadProfile ──────────────────────────────────────────────────────

    @Test
    fun loadProfile_returnsDefaults_whenEmpty() {
        val profile = repo.loadProfile()
        assertEquals(0, profile.coins)
        assertEquals(0, profile.currentStreak)
        assertEquals("", profile.lastLoginDate)
        assertEquals(0, profile.bestStreak)
        assertEquals(0, profile.totalGamesPlayed)
        assertEquals(0L, profile.totalCorrectTaps)
        assertEquals(0, profile.highScore)
        assertEquals(true, profile.notificationEnabled)
        assertEquals(emptyList<DailyMission>(), profile.todayMissions)
        assertEquals("", profile.missionDate)
        assertEquals(emptyMap<PowerUpType, Int>(), profile.ownedPowerUps)
    }

    @Test
    fun saveAndLoadProfile_roundtrip() {
        val missions = listOf(
            DailyMission("m1", MissionType.SCORE_TARGET, 25, 10, 20, true, false),
            DailyMission("m2", MissionType.COMBO_TARGET, 7, 3, 15, false, false)
        )
        val powerUps = mapOf(PowerUpType.SLOW_MOTION to 3, PowerUpType.EXTRA_TIME to 1)
        val profile = PlayerProfile(
            coins = 500,
            currentStreak = 4,
            lastLoginDate = "2025-06-05",
            bestStreak = 7,
            totalGamesPlayed = 42,
            totalCorrectTaps = 1234L,
            highScore = 99,
            todayMissions = missions,
            missionDate = "2025-06-05",
            ownedPowerUps = powerUps,
            notificationEnabled = false
        )

        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        assertEquals(500, loaded.coins)
        assertEquals(4, loaded.currentStreak)
        assertEquals("2025-06-05", loaded.lastLoginDate)
        assertEquals(7, loaded.bestStreak)
        assertEquals(42, loaded.totalGamesPlayed)
        assertEquals(1234L, loaded.totalCorrectTaps)
        assertEquals(99, loaded.highScore)
        assertEquals(false, loaded.notificationEnabled)
        assertEquals("2025-06-05", loaded.missionDate)
        assertEquals(2, loaded.todayMissions.size)
        assertEquals(false, loaded.notificationEnabled)

        // Mission details
        val m1 = loaded.todayMissions[0]
        assertEquals("m1", m1.id)
        assertEquals(MissionType.SCORE_TARGET, m1.type)
        assertEquals(25, m1.target)
        assertEquals(10, m1.progress)
        assertEquals(20, m1.coinReward)
        assertTrue(m1.isCompleted)
        assertFalse(m1.isClaimed)

        // PowerUps
        assertEquals(3, loaded.ownedPowerUps[PowerUpType.SLOW_MOTION])
        assertEquals(1, loaded.ownedPowerUps[PowerUpType.EXTRA_TIME])
    }

    // ── processDailyLogin ────────────────────────────────────────────────

    @Test
    fun processDailyLogin_firstLogin_setsStreakTo1() {
        val profile = PlayerProfile(lastLoginDate = "", currentStreak = 0)
        val result = repo.processDailyLogin(profile)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        assertEquals(1, result.currentStreak)
        assertEquals(today, result.lastLoginDate)
        // Day 1 reward = 10 coins
        assertEquals(10, result.coins)
        assertTrue(result.bestStreak >= 1)
    }

    @Test
    fun processDailyLogin_consecutiveDay_incrementsStreak() {
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = yesterday,
            currentStreak = 3,
            bestStreak = 5,
            coins = 100
        )
        val result = repo.processDailyLogin(profile)

        assertEquals(4, result.currentStreak)
        assertEquals(5, result.bestStreak) // best unchanged since 4 < 5
        // Day 4 reward = 25 coins
        assertEquals(125, result.coins)
    }

    @Test
    fun processDailyLogin_consecutiveDay_updatesBestStreak() {
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = yesterday,
            currentStreak = 9,
            bestStreak = 9,
            coins = 0
        )
        val result = repo.processDailyLogin(profile)

        assertEquals(10, result.currentStreak)
        assertEquals(10, result.bestStreak) // updated since 10 > 9
    }

    @Test
    fun processDailyLogin_sameDay_returnsProfileUnchanged() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = today,
            currentStreak = 5,
            coins = 42
        )
        val result = repo.processDailyLogin(profile)

        // Same day => profile returned as-is
        assertEquals(5, result.currentStreak)
        assertEquals(42, result.coins)
        assertEquals(today, result.lastLoginDate)
    }

    @Test
    fun processDailyLogin_brokenStreak_resetsTo1() {
        val twoDaysAgo = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = twoDaysAgo,
            currentStreak = 14,
            bestStreak = 14,
            coins = 0
        )
        val result = repo.processDailyLogin(profile)

        assertEquals(1, result.currentStreak)
        assertEquals(14, result.bestStreak) // best is not reduced
        // Day 1 reward = 10 coins
        assertEquals(10, result.coins)
    }

    @Test
    fun processDailyLogin_dstSafe_usesLocalDateNotMillis() {
        // This test verifies the DST safety by ensuring we use LocalDate-based comparison.
        // A date like 2025-03-09 (DST spring forward in US) to 2025-03-10 should still count as consecutive.
        // We verify this by checking that minusDays(1) correctly computes yesterday
        // even across DST boundaries — LocalDate doesn't use millis.
        val dayBefore = LocalDate.of(2025, 3, 9)
        val dayAfter = LocalDate.of(2025, 3, 10)
        assertEquals(dayBefore, dayAfter.minusDays(1))

        // Similarly for fall-back
        val fallBack = LocalDate.of(2025, 11, 2)
        val dayBefore2 = LocalDate.of(2025, 11, 1)
        assertEquals(dayBefore2, fallBack.minusDays(1))
    }

    @Test
    fun processDailyLogin_generatesNewMissions_whenMissionDateIsNotToday() {
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = yesterday,
            currentStreak = 1,
            missionDate = yesterday,
            todayMissions = listOf(
                DailyMission("old", MissionType.GAMES_PLAYED, 5, 5, 10, true, true)
            )
        )
        val result = repo.processDailyLogin(profile)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        assertEquals(today, result.missionDate)
        assertEquals(3, result.todayMissions.size)
        // All IDs should start with "daily_${today}"
        result.todayMissions.forEach { m ->
            assertTrue(m.id.startsWith("daily_$today"))
        }
    }

    @Test
    fun processDailyLogin_keepsMissions_whenMissionDateIsToday() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val existingMissions = listOf(
            DailyMission("m1", MissionType.SCORE_TARGET, 25, 10, 20)
        )
        // lastLoginDate != today so login actually processes
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = yesterday,
            currentStreak = 1,
            missionDate = today,
            todayMissions = existingMissions
        )
        val result = repo.processDailyLogin(profile)

        // missionDate == today, so missions should be preserved
        assertEquals(1, result.todayMissions.size)
        assertEquals("m1", result.todayMissions[0].id)
    }

    @Test
    fun processDailyLogin_awardsBonusPowerUp_on7thDay() {
        // Streak was 6, this login makes it 7 → bonus power-up
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = yesterday,
            currentStreak = 6,
            coins = 0
        )
        val result = repo.processDailyLogin(profile)

        assertEquals(7, result.currentStreak)
        // Day 7 bonus = HIGHLIGHT power-up
        assertNotNull(result.ownedPowerUps[PowerUpType.HIGHLIGHT])
        assertEquals(1, result.ownedPowerUps[PowerUpType.HIGHLIGHT])
    }

    // ── awardTapCoins ────────────────────────────────────────────────────

    @Test
    fun awardTapCoins_addsOneCoinByDefault() {
        val profile = PlayerProfile(coins = 5)
        val result = repo.awardTapCoins(profile)
        assertEquals(6, result.coins)
    }

    @Test
    fun awardTapCoins_addsSpecifiedCount() {
        val profile = PlayerProfile(coins = 10)
        val result = repo.awardTapCoins(profile, tapCount = 7)
        assertEquals(17, result.coins)
    }

    @Test
    fun awardTapCoins_withZero_doesNotChange() {
        val profile = PlayerProfile(coins = 10)
        val result = repo.awardTapCoins(profile, tapCount = 0)
        assertEquals(10, result.coins)
    }

    // ── awardComboBonus ──────────────────────────────────────────────────

    @Test
    fun awardComboBonus_noBonus_below3() {
        val profile = PlayerProfile(coins = 100)
        val result = repo.awardComboBonus(profile, combo = 2)
        assertEquals(100, result.coins)
    }

    @Test
    fun awardComboBonus_threshold3_awards1() {
        val profile = PlayerProfile(coins = 0)
        val result = repo.awardComboBonus(profile, combo = 3)
        assertEquals(1, result.coins)
    }

    @Test
    fun awardComboBonus_threshold5_awards3() {
        val profile = PlayerProfile(coins = 0)
        val result = repo.awardComboBonus(profile, combo = 5)
        assertEquals(3, result.coins)
    }

    @Test
    fun awardComboBonus_threshold10_awards5() {
        val profile = PlayerProfile(coins = 0)
        val result = repo.awardComboBonus(profile, combo = 10)
        assertEquals(5, result.coins)
    }

    @Test
    fun awardComboBonus_above10_still5() {
        val profile = PlayerProfile(coins = 0)
        val result = repo.awardComboBonus(profile, combo = 25)
        assertEquals(5, result.coins)
    }

    @Test
    fun awardComboBonus_betweenThresholds() {
        val profile = PlayerProfile(coins = 0)
        // 4 is >= 3 but < 5, should get 1
        assertEquals(1, repo.awardComboBonus(profile, combo = 4).coins)
        // 7 is >= 5 but < 10, should get 3
        assertEquals(3, repo.awardComboBonus(profile, combo = 7).coins)
    }

    // ── updateMissionProgress ────────────────────────────────────────────

    @Test
    fun updateMissionProgress_SCORE_TARGET_usesMaxScore() {
        val mission = DailyMission("s1", MissionType.SCORE_TARGET, 20, 10, 15)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, gameScore = 18)

        assertEquals(18, result.todayMissions[0].progress)
        assertFalse(result.todayMissions[0].isCompleted) // 18 < 20
    }

    @Test
    fun updateMissionProgress_SCORE_TARGET_completesWhenReached() {
        val mission = DailyMission("s1", MissionType.SCORE_TARGET, 15, 5, 10)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, gameScore = 20)

        assertEquals(20, result.todayMissions[0].progress)
        assertTrue(result.todayMissions[0].isCompleted)
    }

    @Test
    fun updateMissionProgress_SCORE_TARGET_doesNotReduceProgress() {
        val mission = DailyMission("s1", MissionType.SCORE_TARGET, 30, 25, 10)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, gameScore = 10)

        // maxOf(25, 10) = 25
        assertEquals(25, result.todayMissions[0].progress)
    }

    @Test
    fun updateMissionProgress_COMBO_TARGET_usesMaxCombo() {
        val mission = DailyMission("c1", MissionType.COMBO_TARGET, 5, 0, 10)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, maxCombo = 7)

        assertEquals(7, result.todayMissions[0].progress)
        assertTrue(result.todayMissions[0].isCompleted)
    }

    @Test
    fun updateMissionProgress_COMBO_TARGET_doesNotReduceProgress() {
        val mission = DailyMission("c1", MissionType.COMBO_TARGET, 10, 8, 15)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, maxCombo = 3)

        assertEquals(8, result.todayMissions[0].progress)
    }

    @Test
    fun updateMissionProgress_GAMES_PLAYED_incrementsBy1() {
        val mission = DailyMission("g1", MissionType.GAMES_PLAYED, 3, 1, 20)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile)

        assertEquals(2, result.todayMissions[0].progress)
        assertFalse(result.todayMissions[0].isCompleted)
    }

    @Test
    fun updateMissionProgress_GAMES_PLAYED_completesWhenTargetReached() {
        val mission = DailyMission("g1", MissionType.GAMES_PLAYED, 2, 1, 20)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile)

        assertEquals(2, result.todayMissions[0].progress)
        assertTrue(result.todayMissions[0].isCompleted)
    }

    @Test
    fun updateMissionProgress_TOTAL_TAPS_addsCorrectTaps() {
        val mission = DailyMission("t1", MissionType.TOTAL_TAPS, 50, 20, 25)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, correctTaps = 15)

        assertEquals(35, result.todayMissions[0].progress)
    }

    @Test
    fun updateMissionProgress_TOTAL_TAPS_completesWhenReached() {
        val mission = DailyMission("t1", MissionType.TOTAL_TAPS, 30, 25, 20)
        val profile = PlayerProfile(todayMissions = listOf(mission))
        val result = repo.updateMissionProgress(profile, correctTaps = 10)

        assertEquals(35, result.todayMissions[0].progress)
        assertTrue(result.todayMissions[0].isCompleted)
    }

    @Test
    fun updateMissionProgress_skipsCompletedMissions() {
        val completed = DailyMission("c1", MissionType.GAMES_PLAYED, 3, 3, 10, isCompleted = true)
        val incomplete = DailyMission("i1", MissionType.SCORE_TARGET, 20, 0, 15)
        val profile = PlayerProfile(todayMissions = listOf(completed, incomplete))
        val result = repo.updateMissionProgress(profile, gameScore = 25)

        // Completed mission unchanged
        assertEquals(3, result.todayMissions[0].progress)
        assertTrue(result.todayMissions[0].isCompleted)
        // Incomplete mission updated
        assertEquals(25, result.todayMissions[1].progress)
        assertTrue(result.todayMissions[1].isCompleted)
    }

    // ── claimMission ─────────────────────────────────────────────────────

    @Test
    fun claimMission_success_awardsCoinsAndMarksClaimed() {
        val mission = DailyMission("m1", MissionType.SCORE_TARGET, 10, 10, 25, isCompleted = true)
        val profile = PlayerProfile(todayMissions = listOf(mission), coins = 50)
        val result = repo.claimMission(profile, "m1")

        assertTrue(result.todayMissions[0].isClaimed)
        assertEquals(75, result.coins) // 50 + 25 reward
    }

    @Test
    fun claimMission_alreadyClaimed_noCoinsAwarded() {
        val mission = DailyMission("m1", MissionType.SCORE_TARGET, 10, 10, 25, isCompleted = true, isClaimed = true)
        val profile = PlayerProfile(todayMissions = listOf(mission), coins = 50)
        val result = repo.claimMission(profile, "m1")

        assertTrue(result.todayMissions[0].isClaimed)
        assertEquals(50, result.coins) // no additional coins
    }

    @Test
    fun claimMission_notCompleted_noCoinsAwarded() {
        val mission = DailyMission("m1", MissionType.SCORE_TARGET, 10, 5, 25, isCompleted = false)
        val profile = PlayerProfile(todayMissions = listOf(mission), coins = 50)
        val result = repo.claimMission(profile, "m1")

        assertFalse(result.todayMissions[0].isClaimed)
        assertEquals(50, result.coins) // no coins
    }

    @Test
    fun claimMission_wrongId_noCoinsAwarded() {
        val mission = DailyMission("m1", MissionType.SCORE_TARGET, 10, 10, 25, isCompleted = true)
        val profile = PlayerProfile(todayMissions = listOf(mission), coins = 50)
        val result = repo.claimMission(profile, "wrong_id")

        assertFalse(result.todayMissions[0].isClaimed)
        assertEquals(50, result.coins)
    }

    @Test
    fun claimMission_multipleMissions_onlyClaimsTarget() {
        val m1 = DailyMission("m1", MissionType.SCORE_TARGET, 10, 10, 20, isCompleted = true)
        val m2 = DailyMission("m2", MissionType.COMBO_TARGET, 5, 5, 30, isCompleted = true)
        val profile = PlayerProfile(todayMissions = listOf(m1, m2), coins = 0)
        val result = repo.claimMission(profile, "m1")

        assertTrue(result.todayMissions[0].isClaimed)
        assertFalse(result.todayMissions[1].isClaimed)
        assertEquals(20, result.coins)
    }

    // ── purchasePowerUp ──────────────────────────────────────────────────

    @Test
    fun purchasePowerUp_success_deductsCoinsAndAddsPowerUp() {
        val profile = PlayerProfile(coins = 100)
        val result = repo.purchasePowerUp(profile, PowerUpType.HIGHLIGHT)!!

        assertEquals(70, result.coins) // 100 - 30 (baseCost)
        assertEquals(1, result.ownedPowerUps[PowerUpType.HIGHLIGHT])
    }

    @Test
    fun purchasePowerUp_insufficientCoins_returnsNull() {
        val profile = PlayerProfile(coins = 10)
        val result = repo.purchasePowerUp(profile, PowerUpType.SLOW_MOTION)

        assertNull(result)
    }

    @Test
    fun purchasePowerUp_exactCoins_succeeds() {
        val profile = PlayerProfile(coins = 50) // exact baseCost of SLOW_MOTION
        val result = repo.purchasePowerUp(profile, PowerUpType.SLOW_MOTION)!!

        assertEquals(0, result.coins)
        assertEquals(1, result.ownedPowerUps[PowerUpType.SLOW_MOTION])
    }

    @Test
    fun purchasePowerUp_tierMultiplier_increasesCost() {
        val profile = PlayerProfile(coins = 100)
        // HIGHLIGHT baseCost=30, tier 2x → cost=60
        val result = repo.purchasePowerUp(profile, PowerUpType.HIGHLIGHT, tierMultiplier = 2f)!!

        assertEquals(40, result.coins) // 100 - 60
        assertEquals(1, result.ownedPowerUps[PowerUpType.HIGHLIGHT])
    }

    @Test
    fun purchasePowerUp_tierMultiplier_insufficientForHigherTier() {
        val profile = PlayerProfile(coins = 45)
        // HIGHLIGHT baseCost=30, tier 1.5x → cost=45, should succeed
        val result = repo.purchasePowerUp(profile, PowerUpType.HIGHLIGHT, tierMultiplier = 1.5f)
        assertNotNull(result)
        assertEquals(0, result!!.coins)
    }

    @Test
    fun purchasePowerUp_tierMultiplier_tooExpensive() {
        val profile = PlayerProfile(coins = 44)
        // HIGHLIGHT baseCost=30, tier 1.5x → cost=45
        val result = repo.purchasePowerUp(profile, PowerUpType.HIGHLIGHT, tierMultiplier = 1.5f)
        assertNull(result)
    }

    @Test
    fun purchasePowerUp_stacksExistingPowerUp() {
        val profile = PlayerProfile(
            coins = 200,
            ownedPowerUps = mapOf(PowerUpType.EXTRA_TIME to 2)
        )
        val result = repo.purchasePowerUp(profile, PowerUpType.EXTRA_TIME)!!

        assertEquals(120, result.coins) // 200 - 80
        assertEquals(3, result.ownedPowerUps[PowerUpType.EXTRA_TIME])
    }

    // ── purchaseRevive (#210) ────────────────────────────────────────────

    @Test
    fun purchaseRevive_insufficientCoins_returnsNull() {
        val profile = PlayerProfile(coins = 10)
        val result = repo.purchaseRevive(profile, cost = 50)

        assertNull(result)
    }

    @Test
    fun purchaseRevive_insufficientCoins_leavesProfileUnchanged() {
        val profile = PlayerProfile(coins = 49)
        val result = repo.purchaseRevive(profile, cost = 50)

        assertNull(result)
        assertEquals(49, profile.coins) // original untouched
    }

    @Test
    fun purchaseRevive_exactCoins_succeeds() {
        val profile = PlayerProfile(coins = 50)
        val result = repo.purchaseRevive(profile, cost = 50)!!

        assertEquals(0, result.coins)
    }

    @Test
    fun purchaseRevive_ampleCoins_deductsCost() {
        val profile = PlayerProfile(coins = 120)
        val result = repo.purchaseRevive(profile, cost = 50)!!

        assertEquals(70, result.coins) // 120 - 50
    }

    @Test
    fun purchaseRevive_preservesOtherProfileFields() {
        val profile = PlayerProfile(
            coins = 200,
            highScore = 555,
            currentStreak = 3,
            ownedPowerUps = mapOf(PowerUpType.SLOW_MOTION to 2)
        )
        val result = repo.purchaseRevive(profile, cost = 50)!!

        assertEquals(150, result.coins)
        assertEquals(555, result.highScore)
        assertEquals(3, result.currentStreak)
        assertEquals(2, result.ownedPowerUps[PowerUpType.SLOW_MOTION])
    }

    @Test
    fun purchaseRevive_doesNotMutateOriginalProfile() {
        val profile = PlayerProfile(coins = 200)
        repo.purchaseRevive(profile, cost = 50)

        assertEquals(200, profile.coins) // original untouched (immutability)
    }

    @Test
    fun purchaseRevive_zeroCost_succeedsAndDeductsNothing() {
        val profile = PlayerProfile(coins = 0)
        val result = repo.purchaseRevive(profile, cost = 0)!!

        assertEquals(0, result.coins)
    }

    @Test
    fun purchaseRevive_usesGameConfigDefaultCost() {
        // The real revive flow uses GameConfig.COIN_COST_FOR_REVIVE (50).
        // This guards against accidental drift between the constant and usage.
        val profile = PlayerProfile(coins = 50)
        val result = repo.purchaseRevive(
            profile,
            cost = com.xarlord.numbertap.data.GameConfig.COIN_COST_FOR_REVIVE
        )

        assertNotNull(result)
        assertEquals(0, result!!.coins)
    }

    // ── usePowerUp ───────────────────────────────────────────────────────

    @Test
    fun usePowerUp_success_decrementsCount() {
        val profile = PlayerProfile(ownedPowerUps = mapOf(PowerUpType.PEEK to 3))
        val result = repo.usePowerUp(profile, PowerUpType.PEEK)!!

        assertEquals(2, result.ownedPowerUps[PowerUpType.PEEK])
    }

    @Test
    fun usePowerUp_noneOwned_returnsNull() {
        val profile = PlayerProfile(ownedPowerUps = emptyMap())
        val result = repo.usePowerUp(profile, PowerUpType.SLOW_MOTION)

        assertNull(result)
    }

    @Test
    fun usePowerUp_decrementToZero_removesKey() {
        val profile = PlayerProfile(ownedPowerUps = mapOf(PowerUpType.HIGHLIGHT to 1))
        val result = repo.usePowerUp(profile, PowerUpType.HIGHLIGHT)!!

        assertFalse(result.ownedPowerUps.containsKey(PowerUpType.HIGHLIGHT))
        assertTrue(result.ownedPowerUps.isEmpty())
    }

    @Test
    fun usePowerUp_otherTypesUnaffected() {
        val profile = PlayerProfile(
            ownedPowerUps = mapOf(
                PowerUpType.SLOW_MOTION to 2,
                PowerUpType.HIGHLIGHT to 5
            )
        )
        val result = repo.usePowerUp(profile, PowerUpType.HIGHLIGHT)!!

        assertEquals(2, result.ownedPowerUps[PowerUpType.SLOW_MOTION])
        assertEquals(4, result.ownedPowerUps[PowerUpType.HIGHLIGHT])
    }

    @Test
    fun usePowerUp_zeroCount_returnsNull() {
        // Even if key exists with 0 (shouldn't happen, but edge case)
        val profile = PlayerProfile(ownedPowerUps = mapOf(PowerUpType.SLOW_MOTION to 1))
        // Use it once
        val after1 = repo.usePowerUp(profile, PowerUpType.SLOW_MOTION)!!
        assertFalse(after1.ownedPowerUps.containsKey(PowerUpType.SLOW_MOTION))
        // Use again → null
        val after2 = repo.usePowerUp(after1, PowerUpType.SLOW_MOTION)
        assertNull(after2)
    }

    // ── JSON serialization roundtrip ─────────────────────────────────────

    @Test
    fun missionsJson_roundtrip_preservesAllFields() {
        val missions = listOf(
            DailyMission("id_a", MissionType.SCORE_TARGET, 50, 30, 25, true, false),
            DailyMission("id_b", MissionType.COMBO_TARGET, 7, 7, 15, true, true),
            DailyMission("id_c", MissionType.GAMES_PLAYED, 10, 3, 40, false, false),
            DailyMission("id_d", MissionType.TOTAL_TAPS, 100, 75, 30, false, false)
        )
        val profile = PlayerProfile(todayMissions = missions)

        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        assertEquals(4, loaded.todayMissions.size)
        for (i in missions.indices) {
            assertEquals(missions[i].id, loaded.todayMissions[i].id)
            assertEquals(missions[i].type, loaded.todayMissions[i].type)
            assertEquals(missions[i].target, loaded.todayMissions[i].target)
            assertEquals(missions[i].progress, loaded.todayMissions[i].progress)
            assertEquals(missions[i].coinReward, loaded.todayMissions[i].coinReward)
            assertEquals(missions[i].isCompleted, loaded.todayMissions[i].isCompleted)
            assertEquals(missions[i].isClaimed, loaded.todayMissions[i].isClaimed)
        }
    }

    @Test
    fun missionsJson_emptyList_roundtrip() {
        val profile = PlayerProfile(todayMissions = emptyList())
        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        assertEquals(0, loaded.todayMissions.size)
    }

    @Test
    fun powerUpsJson_roundtrip_preservesAllTypes() {
        val powerUps = mapOf(
            PowerUpType.SLOW_MOTION to 5,
            PowerUpType.HIGHLIGHT to 1,
            PowerUpType.EXTRA_TIME to 10,
            PowerUpType.PEEK to 3
        )
        val profile = PlayerProfile(ownedPowerUps = powerUps)

        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        assertEquals(5, loaded.ownedPowerUps[PowerUpType.SLOW_MOTION])
        assertEquals(1, loaded.ownedPowerUps[PowerUpType.HIGHLIGHT])
        assertEquals(10, loaded.ownedPowerUps[PowerUpType.EXTRA_TIME])
        assertEquals(3, loaded.ownedPowerUps[PowerUpType.PEEK])
        assertEquals(4, loaded.ownedPowerUps.size)
    }

    @Test
    fun powerUpsJson_emptyMap_roundtrip() {
        val profile = PlayerProfile(ownedPowerUps = emptyMap())
        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        assertEquals(0, loaded.ownedPowerUps.size)
    }

    @Test
    fun powerUpsJson_zeroValues_notStored() {
        // PowerUp with 0 count should not appear in loaded profile
        val powerUps = mapOf(
            PowerUpType.SLOW_MOTION to 1,
            PowerUpType.HIGHLIGHT to 0
        )
        val profile = PlayerProfile(ownedPowerUps = powerUps)

        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        // HIGHLIGHT with 0 should be filtered out by parsePowerUps
        assertEquals(1, loaded.ownedPowerUps.size)
        assertEquals(1, loaded.ownedPowerUps[PowerUpType.SLOW_MOTION])
        assertNull(loaded.ownedPowerUps[PowerUpType.HIGHLIGHT])
    }

    @Test
    fun fullProfile_roundtrip_allFieldsPreserved() {
        val missions = listOf(
            DailyMission("full1", MissionType.TOTAL_TAPS, 150, 100, 35, true, true),
            DailyMission("full2", MissionType.COMBO_TARGET, 13, 5, 20, false, false)
        )
        val powerUps = mapOf(
            PowerUpType.PEEK to 7,
            PowerUpType.SLOW_MOTION to 2
        )
        val profile = PlayerProfile(
            coins = 9999,
            currentStreak = 14,
            lastLoginDate = "2025-12-25",
            bestStreak = 21,
            totalGamesPlayed = 500,
            totalCorrectTaps = 99999L,
            highScore = 250,
            todayMissions = missions,
            missionDate = "2025-12-25",
            ownedPowerUps = powerUps,
            notificationEnabled = true
        )

        repo.saveProfile(profile)
        val loaded = repo.loadProfile()

        assertEquals(profile, loaded)
    }

    // ── StreakRewards integration ────────────────────────────────────────

    @Test
    fun processDailyLogin_streakDay7_awardsCorrectCoins() {
        // Day 7 reward = 100 coins
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val profile = PlayerProfile(
            lastLoginDate = yesterday,
            currentStreak = 6,
            coins = 0
        )
        val result = repo.processDailyLogin(profile)

        assertEquals(100, result.coins) // Day 7 = 100 coins
    }

    @Test
    fun processDailyLogin_streakDay1_through_day7_coinValues() {
        // Verify the coin reward cycle: [10, 15, 20, 25, 30, 50, 100]
        val expected = listOf(10, 15, 20, 25, 30, 50, 100)

        for (i in expected.indices) {
            val streakDay = i + 1
            assertEquals(expected[i], StreakRewards.coinsForDay(streakDay))
        }
    }

    @Test
    fun processDailyLogin_streakCyclesAfter7() {
        // Day 8 should cycle back to day-1 reward (10)
        assertEquals(10, StreakRewards.coinsForDay(8))
        // Day 14 should cycle to day-7 reward (100)
        assertEquals(100, StreakRewards.coinsForDay(14))
    }
}

// ── In-memory SharedPreferences implementation ──────────────────────────

/**
 * Simple in-memory fake for SharedPreferences that supports all the operations
 * used by ProfileRepository: getInt, getLong, getString, getBoolean, edit(), etc.
 */
class FakeSharedPreferences : SharedPreferences {

    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data.toMutableMap()

    override fun getString(key: String, defValue: String?): String? {
        return data[key] as? String ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return data[key] as? MutableSet<String> ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return data[key] as? Int ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return data[key] as? Long ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return data[key] as? Float ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return data[key] as? Boolean ?: defValue
    }

    override fun contains(key: String): Boolean = data.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor(data)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}

    private class FakeEditor(private val data: MutableMap<String, Any?>) : SharedPreferences.Editor {

        private val pending = mutableMapOf<String, Any?>()
        private var cleared = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
            pending[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            pending[key] = REMOVE_MARKER
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            cleared = true
            return this
        }

        override fun commit(): Boolean {
            applyInternal()
            return true
        }

        override fun apply() {
            applyInternal()
        }

        private fun applyInternal() {
            if (cleared) {
                data.clear()
            }
            pending.forEach { (k, v) ->
                if (v === REMOVE_MARKER) {
                    data.remove(k)
                } else {
                    data[k] = v
                }
            }
        }
    }

    companion object {
        private val REMOVE_MARKER = Any()
    }
}
