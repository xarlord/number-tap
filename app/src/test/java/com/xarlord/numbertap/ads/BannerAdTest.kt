package com.xarlord.numbertap.ads

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for BannerAd composable configuration.
 * Issue #147: BannerAd.kt had no test coverage.
 *
 * Note: BannerAd is a Composable that wraps an Android AdView, making it
 * difficult to unit test directly. These tests verify the configuration
 * constants and AdManagerImpl integration it depends on.
 *
 * Full Compose UI tests for BannerAd rendering should be added as
 * instrumented tests (androidTest) when a Compose test environment
 * with AdMob mocks is available.
 */
class BannerAdTest {

    @Test
    fun `banner ad unit ID is correctly configured`() {
        // BannerAd composable uses AdManagerImpl.BANNER_AD_UNIT_ID
        // Verify it's a valid AdMob ad unit ID format
        val adUnitId = AdManagerImpl.BANNER_AD_UNIT_ID
        assertTrue(
            "Banner ad unit ID should start with 'ca-app-pub-'",
            adUnitId.startsWith("ca-app-pub-")
        )
        assertTrue(
            "Banner ad unit ID should contain a slash separating app/pub ID from unit ID",
            adUnitId.contains("/")
        )
    }

    @Test
    fun `banner ad unit ID matches expected value`() {
        // Regression test: ensure the banner ad unit ID hasn't changed unexpectedly
        assertEquals(
            "ca-app-pub-2335615408331368/9191829422",
            AdManagerImpl.BANNER_AD_UNIT_ID
        )
    }

    @Test
    fun `all ad unit IDs are distinct`() {
        // Each ad type should have a unique ad unit ID
        val ids = setOf(
            AdManagerImpl.BANNER_AD_UNIT_ID,
            AdManagerImpl.INTERSTITIAL_AD_UNIT_ID,
            AdManagerImpl.REWARDED_AD_UNIT_ID
        )
        assertEquals("All 3 ad unit IDs should be unique", 3, ids.size)
    }

    @Test
    fun `interstitial frequency is positive`() {
        // BannerAd may not use this directly, but it's part of the ad config
        assertTrue(
            "Interstitial frequency must be positive",
            AdManagerImpl.INTERSTITIAL_FREQUENCY > 0
        )
    }

    @Test
    fun `ad manager stub returns false for banner`() {
        // StubAdManager should return false for banner (no SDK available)
        val stub = StubAdManager()
        assertFalse("StubAdManager.loadBanner() should return false", stub.loadBanner())
    }
}
