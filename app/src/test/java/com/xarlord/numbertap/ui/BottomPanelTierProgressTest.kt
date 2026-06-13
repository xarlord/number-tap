package com.xarlord.numbertap.ui

import com.xarlord.numbertap.data.DifficultyConfig
import com.xarlord.numbertap.data.DifficultyTier
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Tests for [computeTierProgress] — #196.
 *
 * Verifies the tier resolution + progress logic that was previously embedded
 * untested inside the [BottomPanel] composable (#193 refactor). Covers default
 * DifficultyConfig tiers, boundary scores, custom thresholds, the
 * division-by-zero guard, and the max-tier case.
 */
class BottomPanelTierProgressTest {

    @Before
    fun setUp() {
        DifficultyConfig.resetDefaults()
    }

    @After
    fun tearDown() {
        DifficultyConfig.resetDefaults()
    }

    // --- Default-tier coverage ---

    @Test
    fun `score 0 resolves to EASY tier (index 0)`() {
        val info = computeTierProgress(0)
        assertEquals(0, info.tierIndex)
        assertEquals(16, info.nextThreshold)
        // At score 0, range is 16-0 = 16, progress = 0/16 = 0
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `score 8 is halfway through EASY tier`() {
        val info = computeTierProgress(8)
        assertEquals(0, info.tierIndex)
        assertEquals(16, info.nextThreshold)
        // progress = 8/16 = 0.5
        assertEquals(0.5f, info.progress, 0.001f)
    }

    @Test
    fun `score 16 at MEDIUM boundary resolves to tier 1`() {
        val info = computeTierProgress(16)
        assertEquals(1, info.tierIndex)
        assertEquals(41, info.nextThreshold)
        // progress = (16 - 16) / (41 - 16) = 0
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `score 28 is halfway through MEDIUM tier`() {
        val info = computeTierProgress(28)
        assertEquals(1, info.tierIndex)
        assertEquals(41, info.nextThreshold)
        // progress = (28 - 16) / (41 - 16) = 12/25 = 0.48
        assertEquals(0.48f, info.progress, 0.001f)
    }

    @Test
    fun `score 41 at HARD boundary resolves to tier 2`() {
        val info = computeTierProgress(41)
        assertEquals(2, info.tierIndex)
        assertEquals(66, info.nextThreshold)
        // progress = (41 - 41) / (66 - 41) = 0
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `score 53 is halfway through HARD tier`() {
        val info = computeTierProgress(53)
        assertEquals(2, info.tierIndex)
        assertEquals(66, info.nextThreshold)
        // progress = (53 - 41) / (66 - 41) = 12/25 = 0.48
        assertEquals(0.48f, info.progress, 0.001f)
    }

    @Test
    fun `score 66 at INSANE boundary resolves to max tier (index 3)`() {
        val info = computeTierProgress(66)
        assertEquals(3, info.tierIndex)
        assertNull(info.nextThreshold)
        assertEquals(1f, info.progress, 0.001f)
    }

    @Test
    fun `score above max tier stays at INSANE with progress 1f`() {
        val info = computeTierProgress(999)
        assertEquals(3, info.tierIndex)
        assertNull(info.nextThreshold)
        assertEquals(1f, info.progress, 0.001f)
    }

    // --- Custom DifficultyConfig thresholds ---

    @Test
    fun `custom thresholds produce correct tier index and progress`() {
        DifficultyConfig.tiers = listOf(
            DifficultyTier(4, 4, 1.0, 1.5, "EASY", scoreThreshold = 0),
            DifficultyTier(4, 4, 0.7, 2.0, "MEDIUM", scoreThreshold = 10),
            DifficultyTier(5, 5, 0.5, 3.0, "HARD", scoreThreshold = 20),
            DifficultyTier(5, 5, 0.4, 3.5, "INSANE", scoreThreshold = 30, isChaosMode = true)
        )
        val info = computeTierProgress(15)
        assertEquals(1, info.tierIndex)
        assertEquals(20, info.nextThreshold)
        // progress = (15 - 10) / (20 - 10) = 5/10 = 0.5
        assertEquals(0.5f, info.progress, 0.001f)
    }

    // --- Edge cases ---

    @Test
    fun `progress is 0 at lower bound of EASY tier`() {
        val info = computeTierProgress(0)
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `progress never exceeds 1f within a tier`() {
        // Score 15 in EASY tier: progress = 15/16, should be < 1
        val info = computeTierProgress(15)
        assertEquals(0, info.tierIndex)
        assertEquals(0.9375f, info.progress, 0.001f)
    }

    @Test
    fun `division-by-zero guard returns 1f when current and next thresholds are equal`() {
        // Degenerate config where two consecutive tiers share the same threshold
        DifficultyConfig.tiers = listOf(
            DifficultyTier(4, 4, 1.0, 1.5, "EASY", scoreThreshold = 0),
            DifficultyTier(4, 4, 0.7, 2.0, "MEDIUM", scoreThreshold = 0)  // same threshold!
        )
        val info = computeTierProgress(0)
        // range = 0 - 0 = 0, guard returns 1f
        assertEquals(1f, info.progress, 0.001f)
    }

    @Test
    fun `single tier config has null next threshold and progress 1f`() {
        DifficultyConfig.tiers = listOf(
            DifficultyTier(4, 4, 1.0, 1.5, "EASY", scoreThreshold = 0)
        )
        val info = computeTierProgress(0)
        assertEquals(0, info.tierIndex)
        assertNull(info.nextThreshold)
        assertEquals(1f, info.progress, 0.001f)
    }
}
