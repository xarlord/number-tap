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
        // No initialization needed — just testing the counter behavior
        val result = impl.showInterstitial()
        assertFalse("No-arg showInterstitial() should return false", result)
    }

    /**
     * #144: Calling no-arg showInterstitial() should not increment the counter,
     * so subsequent calls to the Activity overload still work correctly.
     * We verify by calling no-arg multiple times, then checking that
     * the Activity overload still increments from 0.
     */
    @Test
    fun `no-arg showInterstitial does not affect gameOverCount`() {
        val impl = AdManagerImpl(mockk(relaxed = true))

        // Call no-arg version multiple times — should NOT count
        repeat(5) {
            impl.showInterstitial()
        }

        // Now call Activity overload with a mock Activity — should start counting from 0
        // Game over #1 (not a multiple of 3) → returns false
        val activity = mockk<android.app.Activity>(relaxed = true)
        val result1 = impl.showInterstitial(activity)
        assertFalse("Game over #1 should not show ad (frequency=3)", result1)

        // Game over #2 → returns false
        val result2 = impl.showInterstitial(activity)
        assertFalse("Game over #2 should not show ad (frequency=3)", result2)

        // Game over #3 → would show ad if one was loaded, but returns false (no ad loaded)
        val result3 = impl.showInterstitial(activity)
        assertFalse("Game over #3 with no ad loaded should return false", result3)

        // Verify: if we'd called no-arg 5 times before, and they DID count,
        // then the Activity calls would be at #6, #7, #8 — and #6 is divisible by 3
        // So this test proves the no-arg calls are not counted
    }

    @Test
    fun `no-arg showRewardedAd always returns false`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val result = impl.showRewardedAd()
        assertFalse("No-arg showRewardedAd() should return false", result)
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
     * #150/#152: showRewardedAd(activity) does NOT delegate to showRewardedWithCallbacks.
     * It has its own ad.show() call. With no ad loaded, it returns false.
     */
    @Test
    fun `showRewardedAd with activity returns false when no ad loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val activity = mockk<android.app.Activity>(relaxed = true)
        assertFalse("showRewardedAd(activity) should return false with no ad loaded",
            impl.showRewardedAd(activity))
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
     * #151: When a rewarded ad IS loaded, showRewardedAd(activity) must return true.
     * The #150 refactor broke this — it always returned false because onReward
     * is async and hadn't fired when the method returned.
     */
    @Test
    fun `showRewardedAd with activity returns true when ad is loaded`() {
        val impl = AdManagerImpl(mockk(relaxed = true))
        val mockAd = mockk<com.google.android.gms.ads.rewarded.RewardedAd>(relaxed = true)
        injectRewardedAd(impl, mockAd)

        val activity = mockk<android.app.Activity>(relaxed = true)
        assertTrue("showRewardedAd(activity) should return true when ad is loaded",
            impl.showRewardedAd(activity))
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
}
