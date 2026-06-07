package com.xarlord.numbertap.ads

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AdManager interface and StubAdManager contract.
 * Issue #15: Verify interface contract — all stub methods return false.
 */
class AdManagerTest {

    private val adManager: AdManager = StubAdManager()

    @Test
    fun `stub loadBanner returns false`() {
        assertFalse(adManager.loadBanner())
    }

    @Test
    fun `stub showInterstitial returns false`() {
        assertFalse(adManager.showInterstitial())
    }

    @Test
    fun `stub showRewardedAd returns false`() {
        assertFalse(adManager.showRewardedAd())
    }

    @Test
    fun `stub isAdReady returns false`() {
        assertFalse(adManager.isAdReady())
    }

    @Test
    fun `stub implements AdManager interface`() {
        assertTrue(adManager is AdManager)
    }

    @Test
    fun `interface has expected method count`() {
        val methods = AdManager::class.java.methods
            .map { it.name }
            .toSet()
        assertTrue(methods.contains("loadBanner"))
        assertTrue(methods.contains("showInterstitial"))
        assertTrue(methods.contains("showRewardedAd"))
        assertTrue(methods.contains("isAdReady"))
    }

    @Test
    fun `stub is consistent across calls`() {
        assertFalse(adManager.isAdReady())
        assertFalse(adManager.isAdReady())
        assertFalse(adManager.loadBanner())
        assertFalse(adManager.isAdReady())
    }
}
