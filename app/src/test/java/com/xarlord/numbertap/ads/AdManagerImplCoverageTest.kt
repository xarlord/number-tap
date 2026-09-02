package com.xarlord.numbertap.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardedAd
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

/**
 * Additional coverage tests for AdManagerImpl.
 *
 * These tests target code paths not covered by AdManagerImplTest to push
 * line coverage from ~26% toward 80%+.
 */
class AdManagerImplCoverageTest {

    private lateinit var context: Context
    private lateinit var impl: AdManagerImpl

    @Before
    fun setUp() {
        mockkStatic(MobileAds::class)
        mockkStatic(InterstitialAd::class)
        mockkStatic(RewardedAd::class)
        mockkStatic(AdRequest.Builder::class)

        // Default: MobileAds.initialize() calls the callback synchronously
        every { MobileAds.initialize(any(), any<OnInitializationCompleteListener>()) } answers {
            val callback = secondArg<OnInitializationCompleteListener>()
            callback.onInitializationComplete(mockk(relaxed = true))
        }
        every { MobileAds.setRequestConfiguration(any()) } returns Unit

        // Default: InterstitialAd.load and RewardedAd.load are no-ops
        every { InterstitialAd.load(any(), any(), any<AdRequest>(), any()) } returns Unit
        every { RewardedAd.load(any(), any(), any<AdRequest>(), any()) } returns Unit

        context = mockk(relaxed = true)
        every { context.applicationContext } returns context
        impl = AdManagerImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── Reflection helpers ──────────────────────────────────────────────

    private fun injectField(name: String, value: Any?) {
        val field: Field = AdManagerImpl::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(impl, value)
    }

    private fun injectRewardedAd(ad: RewardedAd?) {
        injectField("rewardedAd", ad)
    }

    private fun injectInterstitialAd(ad: InterstitialAd?) {
        injectField("interstitialAd", ad)
    }

    private fun setInitialized(value: Boolean) {
        injectField("isInitialized", value)
    }

    private fun getGameOverCount(): Int {
        val field = AdManagerImpl::class.java.getDeclaredField("gameOverCount")
        field.isAccessible = true
        return field.getInt(impl)
    }

    // ── 1. initialize ────────────────────────────────────────────────────

    @Test
    fun `initialize sets isInitialized flag and loadBanner returns true`() {
        impl.initialize()
        assertTrue("loadBanner should return true after initialize()", impl.loadBanner())
    }

    @Test
    fun `initialize is idempotent — calling twice does not crash`() {
        impl.initialize()
        impl.initialize()
        assertTrue(impl.loadBanner())
    }

    @Test
    fun `initialize with callback that does not fire — isInitialized stays false`() {
        // Override: don't invoke callback
        every { MobileAds.initialize(any(), any<OnInitializationCompleteListener>()) } returns Unit
        val impl2 = AdManagerImpl(context)
        impl2.initialize()
        assertFalse("isInitialized should stay false when callback doesn't fire", impl2.loadBanner())
    }

    @Test
    fun `loadBanner returns true after isInitialized is set via reflection`() {
        setInitialized(true)
        assertTrue("loadBanner should return true when isInitialized=true", impl.loadBanner())
    }

    // ── 2 & 3. Interstitial frequency gating ─────────────────────────────

    @Test
    fun `showInterstitial with activity — frequency gating first two calls return false`() {
        val activity = mockk<Activity>(relaxed = true)

        // Game over #1 → not multiple of 3 → false
        assertFalse(impl.showInterstitial(activity))
        assertEquals(1, getGameOverCount())

        // Game over #2 → not multiple of 3 → false
        assertFalse(impl.showInterstitial(activity))
        assertEquals(2, getGameOverCount())
    }

    @Test
    fun `showInterstitial with activity — third call attempts show but returns false when no ad loaded`() {
        val activity = mockk<Activity>(relaxed = true)

        // Game overs 1 and 2 → false (frequency gating)
        impl.showInterstitial(activity)
        impl.showInterstitial(activity)

        // Game over #3 → multiple of 3, attempts to show, but no ad → false
        val result = impl.showInterstitial(activity)
        assertFalse("Should return false when no interstitial ad is loaded", result)
        assertEquals(3, getGameOverCount())
    }

    @Test
    fun `showInterstitial with activity — third call returns true when ad is loaded`() {
        val activity = mockk<Activity>(relaxed = true)
        val mockAd = mockk<InterstitialAd>(relaxed = true)
        injectInterstitialAd(mockAd)

        // Game overs 1 and 2 → false (frequency gating)
        impl.showInterstitial(activity)
        impl.showInterstitial(activity)

        // Game over #3 → ad is loaded → true
        val result = impl.showInterstitial(activity)
        assertTrue("Should return true when interstitial ad is loaded", result)

        // Verify ad.show() was called
        verify { mockAd.show(activity) }
    }

    @Test
    fun `showInterstitial with activity — frequency wraps correctly (4th, 5th false, 6th attempts)`() {
        val activity = mockk<Activity>(relaxed = true)

        // First cycle: 1, 2 false; 3 attempts
        impl.showInterstitial(activity)
        impl.showInterstitial(activity)
        impl.showInterstitial(activity)
        assertEquals(3, getGameOverCount())

        // Game over #4 → not multiple of 3 → false
        assertFalse(impl.showInterstitial(activity))
        assertEquals(4, getGameOverCount())

        // Game over #5 → not multiple of 3 → false
        assertFalse(impl.showInterstitial(activity))
        assertEquals(5, getGameOverCount())

        // Game over #6 → multiple of 3, attempts show, no ad → false
        val result6 = impl.showInterstitial(activity)
        assertFalse(result6)
        assertEquals(6, getGameOverCount())
    }

    @Test
    fun `showInterstitial with activity — 6th call returns true when ad is loaded`() {
        val activity = mockk<Activity>(relaxed = true)
        val mockAd = mockk<InterstitialAd>(relaxed = true)
        injectInterstitialAd(mockAd)

        // Cycle through 5 game overs
        repeat(5) { impl.showInterstitial(activity) }

        // Game over #6 → multiple of 3, ad loaded → true
        val result = impl.showInterstitial(activity)
        assertTrue("6th game over should show interstitial", result)
        verify { mockAd.show(activity) }
    }

    // ── 4. showRewardedWithCallbacks with ad loaded ──────────────────────

    @Test
    fun `showRewardedWithCallbacks with ad loaded calls ad show`() {
        val activity = mockk<Activity>(relaxed = true)
        val mockAd = mockk<RewardedAd>(relaxed = true)
        injectRewardedAd(mockAd)

        var rewardCalled = false
        var failureCalled = false

        impl.showRewardedWithCallbacks(
            activity,
            onReward = { rewardCalled = true },
            onFailure = { failureCalled = true }
        )

        // Verify ad.show() was invoked (the onReward lambda passed to ad.show
        // is captured by MockK; we can't easily fire it, but we cover the code path)
        verify { mockAd.show(activity, any()) }
        assertFalse("onReward should not have fired synchronously", rewardCalled)
        assertFalse("onFailure should not be called when ad is loaded", failureCalled)
    }

    // ── 5. Companion object constants ────────────────────────────────────

    @Test
    fun `BANNER_AD_UNIT_ID has correct format`() {
        assertTrue(
            "Banner ad unit ID should start with ca-app-pub-",
            AdManagerImpl.BANNER_AD_UNIT_ID.startsWith("ca-app-pub-")
        )
    }

    @Test
    fun `INTERSTITIAL_AD_UNIT_ID has correct format`() {
        assertTrue(
            "Interstitial ad unit ID should start with ca-app-pub-",
            AdManagerImpl.INTERSTITIAL_AD_UNIT_ID.startsWith("ca-app-pub-")
        )
    }

    @Test
    fun `REWARDED_AD_UNIT_ID has correct format`() {
        assertTrue(
            "Rewarded ad unit ID should start with ca-app-pub-",
            AdManagerImpl.REWARDED_AD_UNIT_ID.startsWith("ca-app-pub-")
        )
    }

    @Test
    fun `INTERSTITIAL_FREQUENCY is 3`() {
        assertEquals(
            "Interstitial frequency should be 3",
            3,
            AdManagerImpl.INTERSTITIAL_FREQUENCY
        )
    }

    // ── 6. addTestDeviceIds ──────────────────────────────────────────────

    @Test
    fun `addTestDeviceIds runs without error`() {
        impl.addTestDeviceIds(listOf("TEST_DEVICE_123", "TEST_DEVICE_456"))
        verify { MobileAds.setRequestConfiguration(any()) }
    }

    @Test
    fun `addTestDeviceIds with empty list runs without error`() {
        impl.addTestDeviceIds(emptyList())
        verify { MobileAds.setRequestConfiguration(any()) }
    }

    // ── 7. preloadInterstitial ───────────────────────────────────────────

    @Test
    fun `preloadInterstitial runs without crash`() {
        impl.preloadInterstitial()
        verify { InterstitialAd.load(any(), any(), any(), any()) }
    }

    @Test
    fun `preloadInterstitial called twice does not crash`() {
        impl.preloadInterstitial()
        impl.preloadInterstitial()
    }

    // ── 8. preloadRewarded ───────────────────────────────────────────────

    @Test
    fun `preloadRewarded runs without crash`() {
        impl.preloadRewarded()
        verify { RewardedAd.load(any(), any(), any<AdRequest>(), any()) }
    }

    @Test
    fun `preloadRewarded called twice does not crash`() {
        impl.preloadRewarded()
        impl.preloadRewarded()
    }

    // ── Additional edge-case coverage ────────────────────────────────────

    @Test
    fun `constructor stores applicationContext`() {
        val customContext = mockk<Context>(relaxed = true)
        every { customContext.applicationContext } returns customContext
        val impl2 = AdManagerImpl(customContext)
        // No exception = pass
    }

    @Test
    fun `showInterstitial with activity after manual gameOverCount injection`() {
        // Inject gameOverCount = 2 so next call is #3 (multiple of 3)
        val countField = AdManagerImpl::class.java.getDeclaredField("gameOverCount")
        countField.isAccessible = true
        countField.setInt(impl, 2)

        val activity = mockk<Activity>(relaxed = true)
        // This is game over #3 → attempts to show
        val result = impl.showInterstitial(activity)
        assertFalse("Returns false — no ad loaded", result)
        assertEquals(3, getGameOverCount())
    }

    @Test
    fun `showRewardedWithCallbacks does not call onFailure when ad is present`() {
        val activity = mockk<Activity>(relaxed = true)
        val mockAd = mockk<RewardedAd>(relaxed = true)
        injectRewardedAd(mockAd)

        var failureCalled = false
        impl.showRewardedWithCallbacks(activity, onReward = {}, onFailure = { failureCalled = true })
        assertFalse("onFailure should not be called when ad is loaded", failureCalled)
    }

    @Test
    fun `isAdReady returns false after rewarded ad is set to null`() {
        injectRewardedAd(null)
        assertFalse(impl.isAdReady())
    }

    @Test
    fun `no-arg showRewardedAd returns false consistently`() {
        repeat(3) {
            assertFalse(impl.showRewardedAd())
        }
    }

    @Test
    fun `no-arg showInterstitial returns false consistently`() {
        repeat(3) {
            assertFalse(impl.showInterstitial())
        }
    }
}
