package com.xarlord.numbertap.retention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionGeneratorTest {

    @Test
    fun `generate returns exactly 3 missions`() {
        val missions = MissionGenerator.generate("2026-06-10")
        assertEquals(3, missions.size)
    }

    @Test
    fun `generated missions have distinct types`() {
        val missions = MissionGenerator.generate("2026-06-10")
        val types = missions.map { it.type }.distinct()
        assertEquals(3, types.size)
    }

    @Test
    fun `generated missions have correct id format`() {
        val dateSeed = "2026-06-10"
        val missions = MissionGenerator.generate(dateSeed)
        missions.forEachIndexed { i, m ->
            assertEquals("daily_${dateSeed}_$i", m.id)
        }
    }

    @Test
    fun `same date seed produces same missions`() {
        val m1 = MissionGenerator.generate("2026-01-01")
        val m2 = MissionGenerator.generate("2026-01-01")
        assertEquals(m1.map { it.type to it.target }, m2.map { it.type to it.target })
    }

    @Test
    fun `different date seeds can produce different missions`() {
        val m1 = MissionGenerator.generate("2026-01-01")
        val m2 = MissionGenerator.generate("2026-06-15")
        // Very unlikely to be identical
        val same = m1.map { it.type to it.target } == m2.map { it.type to it.target }
        assertTrue("Different dates should produce different missions", !same)
    }

    @Test
    fun `all missions have positive coin rewards`() {
        val missions = MissionGenerator.generate("2026-06-10")
        missions.forEach { m ->
            assertTrue("Reward should be positive, got ${m.coinReward}", m.coinReward > 0)
        }
    }

    @Test
    fun `all missions have positive targets`() {
        val missions = MissionGenerator.generate("2026-06-10")
        missions.forEach { m ->
            assertTrue("Target should be positive, got ${m.target}", m.target > 0)
        }
    }
}
