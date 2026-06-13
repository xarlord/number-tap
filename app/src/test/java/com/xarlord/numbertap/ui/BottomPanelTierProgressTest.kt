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
    fun `score 0 resolves to NORMAL tier (index 0)`() {
        val info = computeTierProgress(0)
        assertEquals(0, info.tierIndex)
        assertEquals(20, info.nextThreshold)
        // At score 0, range is 20-0 = 20, progress = 0/20 = 0
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `score 10 is halfway through NORMAL tier`() {
        val info = computeTierProgress(10)
        assertEquals(0, info.tierIndex)
        assertEquals(20, info.nextThreshold)
        // progress = 10/20 = 0.5
        assertEquals(0.5f, info.progress, 0.001f)
    }

    @Test
    fun `score 20 at FLIP boundary resolves to tier 1`() {
        val info = computeTierProgress(20)
        assertEquals(1, info.tierIndex)
        assertEquals(50, info.nextThreshold)
        // progress = (20 - 20) / (50 - 20) = 0
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `score 35 is halfway through FLIP tier`() {
        val info = computeTierProgress(35)
        assertEquals(1, info.tierIndex)
        assertEquals(50, info.nextThreshold)
        // progress = (35 - 20) / (50 - 20) = 15/30 = 0.5
        assertEquals(0.5f, info.progress, 0.001f)
    }

    @Test
    fun `score 50 at EXPERT boundary resolves to tier 2`() {
        val info = computeTierProgress(50)
        assertEquals(2, info.tierIndex)
        assertEquals(100, info.nextThreshold)
        // progress = (50 - 50) / (100 - 50) = 0
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `score 75 is halfway through EXPERT tier`() {
        val info = computeTierProgress(75)
        assertEquals(2, info.tierIndex)
        assertEquals(100, info.nextThreshold)
        // progress = (75 - 50) / (100 - 50) = 25/50 = 0.5
        assertEquals(0.5f, info.progress, 0.001f)
    }

    @Test
    fun `score 100 at INSANE boundary resolves to max tier (index 3)`() {
        val info = computeTierProgress(100)
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
            DifficultyTier(4, 4, 1.0, 1.5, "NORMAL", scoreThreshold = 0),
            DifficultyTier(4, 4, 0.8, 2.0, "FLIP", scoreThreshold = 10, shouldFlipTiles = true),
            DifficultyTier(5, 5, 0.6, 2.5, "EXPERT", scoreThreshold = 20, shouldFlipTiles = true),
            DifficultyTier(6, 6, 0.5, 3.0, "INSANE", scoreThreshold = 30, shouldFlipTiles = true, isChaosMode = true)
        )
        val info = computeTierProgress(15)
        assertEquals(1, info.tierIndex)
        assertEquals(20, info.nextThreshold)
        // progress = (15 - 10) / (20 - 10) = 5/10 = 0.5
        assertEquals(0.5f, info.progress, 0.001f)
    }

    // --- Edge cases ---

    @Test
    fun `progress is 0 at lower bound of NORMAL tier`() {
        val info = computeTierProgress(0)
        assertEquals(0f, info.progress, 0.001f)
    }

    @Test
    fun `progress never exceeds 1f within a tier`() {
        // Score 19 in NORMAL tier: progress = 19/20, should be < 1
        val info = computeTierProgress(19)
        assertEquals(0, info.tierIndex)
        assertEquals(0.95f, info.progress, 0.001f)
    }

    @Test
    fun `division-by-zero guard returns 1f when current and next thresholds are equal`() {
        // Degenerate config where two consecutive tiers share the same threshold
        DifficultyConfig.tiers = listOf(
            DifficultyTier(4, 4, 1.0, 1.5, "NORMAL", scoreThreshold = 0),
            DifficultyTier(4, 4, 0.8, 2.0, "FLIP", scoreThreshold = 0, shouldFlipTiles = true)  // same threshold!
        )
        val info = computeTierProgress(0)
        // range = 0 - 0 = 0, guard returns 1f
        assertEquals(1f, info.progress, 0.001f)
    }

    @Test
    fun `single tier config has null next threshold and progress 1f`() {
        DifficultyConfig.tiers = listOf(
            DifficultyTier(4, 4, 1.0, 1.5, "NORMAL", scoreThreshold = 0)
        )
        val info = computeTierProgress(0)
        assertEquals(0, info.tierIndex)
        assertNull(info.nextThreshold)
        assertEquals(1f, info.progress, 0.001f)
    }
}
