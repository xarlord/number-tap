package com.xarlord.numbertap.retention

import kotlin.random.Random

/**
 * Generates 3 daily missions with varied types and targets.
 * Issue #89: Daily missions system.
 */
object MissionGenerator {

    fun generate(dateSeed: String): List<DailyMission> {
        val seed = dateSeed.hashCode().toLong()
        val random = Random(seed)

        val missionPool = listOf(
            { r: Random -> makeMission(r, MissionType.SCORE_TARGET, intArrayOf(10, 15, 20, 25, 30, 40, 50), 10..30) },
            { r: Random -> makeMission(r, MissionType.COMBO_TARGET, intArrayOf(3, 5, 7, 10, 13), 15..25) },
            { r: Random -> makeMission(r, MissionType.GAMES_PLAYED, intArrayOf(3, 5, 7, 10), 20..40) },
            { r: Random -> makeMission(r, MissionType.TOTAL_TAPS, intArrayOf(30, 50, 75, 100, 150), 15..35) }
        )

        // Pick 3 distinct mission types
        val shuffled = missionPool.shuffled(random)
        return shuffled.take(3).mapIndexed { i, gen ->
            gen(random).copy(id = "daily_${dateSeed}_$i")
        }
    }

    private fun makeMission(
        random: Random,
        type: MissionType,
        targets: IntArray,
        rewardRange: IntRange
    ): DailyMission {
        val target = targets.random(random)
        val reward = rewardRange.random(random)
        return DailyMission(
            id = "",
            type = type,
            target = target,
            coinReward = reward
        )
    }
}
