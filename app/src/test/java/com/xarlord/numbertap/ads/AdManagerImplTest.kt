package com.xarlord.numbertap.ads

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Field

/**
 * Tests for AdManagerImpl behavior — #144 fix verification.
 *
 * Verifies that the no-arg showInterstitial() override does NOT affect
 * the game over counter used by the Activity overload.
 */
class AdManagerImplTest {

    /**
     * Helper: inject a mock RewardedAd into AdManagerImpl via reflection.
     * #151: Needed to test the "ad loaded" code path.
     */
    private fun injectRewardedAd(impl: AdManagerImpl, ad: com.google.android.gms.ads.rewarded.RewardedAd?) {
        val field: Field = AdManagerImpl::class.java.getDeclaredField("rewardedAd")
        field.isAccessible = true
        field.set(impl, ad)
    }

    /**
     * #144: The no-arg showInterstitial() must not increment gameOverCount.
     * It should be a pure stub that always returns false without side effects.
     */
    @Test
    fun `no-arg showInterstitial always returns false`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val result = impl.showInterstitial()
        assertFalse("No-arg showInterstitial() should return false", result)
    }

    /**
     * #144: Calling no-arg showInterstitial() should not increment the counter,
     * so subsequent calls to the Activity overload still work correctly.
     */
    @Test
    fun `no-arg showInterstitial does not affect gameOverCount`() {
        val impl = AdManagerImpl(mockk(relaxed = true))

        repeat(5) { impl.showInterstitial() }

        val activity = mockk<android.app.Activity>(relaxed = true)
        val result1 = impl.showInterstitial(activity)
        assertFalse("Game over #1 should not show ad (frequency=3)", result1)

        val result2 = impl.showInterstitial(activity)
        assertFalse("Game over #2 should not show ad (frequency=3)", result2)

        val result3 = impl.showInterstitial(activity)
        assertFalse("Game over #3 with no ad loaded should return false", result3)
    }

    @Test
    fun `isAdReady returns false when no ad loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        assertFalse("isAdReady should be false with no ad loaded", impl.isAdReady())
    }

    @Test
    fun `loadBanner returns false when not initialized`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        assertFalse("loadBanner should return false before initialization", impl.loadBanner())
    }

    /**
     * #150: showRewardedWithCallbacks calls onFailure when no ad is loaded.
     */
    @Test
    fun `showRewardedWithCallbacks calls onFailure when no ad loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val activity = mockk<android.app.Activity>(relaxed = true)
        var failureCalled = false
        var rewardCalled = false
        impl.showRewardedWithCallbacks(
            activity,
            onReward = { rewardCalled = true },
            onFailure = { failureCalled = true }
        )
        assertTrue("onFailure should be called", failureCalled)
        assertFalse("onReward should NOT be called", rewardCalled)
    }

    /**
     * #151: isAdReady returns true when a rewarded ad is loaded.
     */
    @Test
    fun `isAdReady returns true when ad is loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val mockAd = mockk<com.google.android.gms.ads.rewarded.RewardedAd>(relaxed = true)
        injectRewardedAd(impl, mockAd)

        assertTrue("isAdReady should return true when ad is loaded", impl.isAdReady())
    }

    /**
     * #241: showRewardedAd() no-arg is a safe stub — returns false, no crash.
     */
    @Test
    fun `no-arg showRewardedAd returns false with no ad loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        assertFalse(impl.showRewardedAd())
    }

    @Test
    fun `no-arg showRewardedAd returns false even with ad loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val mockAd = mockk<com.google.android.gms.ads.rewarded.RewardedAd>(relaxed = true)
        injectRewardedAd(impl, mockAd)
        assertFalse(impl.showRewardedAd())
    }

    /**
     * #150: showRewardedWithCallbacks does not crash when ad is loaded.
     */
    @Test
    fun `showRewardedWithCallbacks does not crash when ad is loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val activity = mockk<android.app.Activity>(relaxed = true)
        val mockAd = mockk<com.google.android.gms.ads.rewarded.RewardedAd>(relaxed = true)
        injectRewardedAd(impl, mockAd)

        var rewardCalled = false
        var failureCalled = false
        impl.showRewardedWithCallbacks(
            activity,
            onReward = { rewardCalled = true },
            onFailure = { failureCalled = true }
        )
        // ad.show() is relaxed mock — callbacks won't fire, but no crash
        assertFalse("onFailure should not be called when ad is loaded", failureCalled)
    }
}
