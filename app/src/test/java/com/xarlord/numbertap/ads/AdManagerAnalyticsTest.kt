package com.xarlord.numbertap.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.xarlord.numbertap.analytics.AnalyticsEvent
import com.xarlord.numbertap.analytics.AnalyticsTracker
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Field

class AdManagerAnalyticsTest {

    @After
    fun tearDown() = unmockkAll()

    private fun manager(): AdManagerImpl {
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        mockkObject(AnalyticsTracker)
        every { AnalyticsTracker.track(any()) } just Runs
        every { AnalyticsTracker.track(any(), any()) } just Runs
        return AdManagerImpl(context, rewardedReloadOverride = {})
    }

    private fun injectInterstitial(manager: AdManagerImpl, ad: InterstitialAd) {
        val field: Field = AdManagerImpl::class.java.getDeclaredField("interstitialAd")
        field.isAccessible = true
        field.set(manager, ad)
    }

    private fun injectRewarded(manager: AdManagerImpl, ad: RewardedAd) {
        val field: Field = AdManagerImpl::class.java.getDeclaredField("rewardedAd")
        field.isAccessible = true
        field.set(manager, ad)
    }

    @Test
    fun `interstitial shown is emitted only from SDK shown callback`() {
        val manager = manager()
        val ad = mockk<InterstitialAd>(relaxed = true)
        injectInterstitial(manager, ad)

        repeat(AdManagerImpl.INTERSTITIAL_FREQUENCY) {
            manager.showInterstitial(mockk<Activity>(relaxed = true))
        }

        verify(exactly = 0) { AnalyticsTracker.track(AnalyticsEvent.AD_INTERSTITIAL_SHOWN) }
        manager.createInterstitialFullScreenCallback().onAdShowedFullScreenContent()
        verify(exactly = 1) { AnalyticsTracker.track(AnalyticsEvent.AD_INTERSTITIAL_SHOWN) }
    }

    @Test
    fun `rewarded no-ready path emits failed once`() {
        val manager = manager()

        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = {},
            onFailure = {}
        )

        verify(exactly = 1) {
            AnalyticsTracker.track(
                AnalyticsEvent.AD_REWARDED_FAILED,
                match { it["failure_stage"] == "not_ready" }
            )
        }
    }

    @Test
    fun `rewarded load failure emits failed once`() {
        val manager = manager()

        manager.createRewardedLoadCallback()
            .onAdFailedToLoad(mockk<LoadAdError>(relaxed = true))

        verify(exactly = 1) {
            AnalyticsTracker.track(
                AnalyticsEvent.AD_REWARDED_FAILED,
                match { it["failure_stage"] == "load" }
            )
        }
    }

    @Test
    fun `overlapping rewarded request cannot replace active failure callback`() {
        val manager = manager()
        val ad = mockk<RewardedAd>(relaxed = true)
        every { ad.show(any(), any<OnUserEarnedRewardListener>()) } just Runs
        injectRewarded(manager, ad)
        var firstFailures = 0
        var secondFailures = 0

        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = {},
            onFailure = { firstFailures++ }
        )
        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = {},
            onFailure = { secondFailures++ }
        )

        assertEquals(0, firstFailures)
        assertEquals(1, secondFailures)

        manager.createRewardedFullScreenCallback()
            .onAdFailedToShowFullScreenContent(mockk<AdError>(relaxed = true))
        assertEquals(1, firstFailures)
        assertEquals(1, secondFailures)
    }

    @Test
    fun `synchronous rewarded show failure resets guard and notifies caller`() {
        val manager = manager()
        val ad = mockk<RewardedAd>()
        every { ad.show(any(), any<OnUserEarnedRewardListener>()) } throws IllegalStateException("show failed")
        injectRewarded(manager, ad)
        var failures = 0

        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = {},
            onFailure = { failures++ }
        )
        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = {},
            onFailure = { failures++ }
        )

        assertEquals(2, failures)
        verify(exactly = 2) {
            AnalyticsTracker.track(
                AnalyticsEvent.AD_REWARDED_FAILED,
                match { it["failure_stage"] == "show" }
            )
        }
    }

    @Test
    fun `rewarded SDK callbacks emit shown earned and failed at authoritative points`() {
        val manager = manager()
        val ad = mockk<RewardedAd>(relaxed = true)
        val fullScreenCallback = slot<com.google.android.gms.ads.FullScreenContentCallback>()
        every { ad.fullScreenContentCallback = capture(fullScreenCallback) } just Runs
        val rewardListener = slot<OnUserEarnedRewardListener>()
        every { ad.show(any(), capture(rewardListener)) } just Runs
        manager.createRewardedLoadCallback().onAdLoaded(ad)
        var rewards = 0
        var failures = 0

        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = { rewards++ },
            onFailure = { failures++ }
        )

        verify(exactly = 0) { AnalyticsTracker.track(AnalyticsEvent.AD_REWARDED_SHOWN) }
        verify(exactly = 0) { AnalyticsTracker.track(AnalyticsEvent.AD_REWARDED_EARNED) }

        fullScreenCallback.captured.onAdShowedFullScreenContent()
        rewardListener.captured.onUserEarnedReward(mockk<RewardItem>(relaxed = true))

        verify(exactly = 1) { AnalyticsTracker.track(AnalyticsEvent.AD_REWARDED_SHOWN) }
        verify(exactly = 1) { AnalyticsTracker.track(AnalyticsEvent.AD_REWARDED_EARNED) }
        assertEquals(1, rewards)

        manager.showRewardedWithCallbacks(
            mockk(relaxed = true),
            onReward = { rewards++ },
            onFailure = { failures++ }
        )
        fullScreenCallback.captured
            .onAdFailedToShowFullScreenContent(mockk<AdError>(relaxed = true))
        verify(exactly = 1) {
            AnalyticsTracker.track(
                AnalyticsEvent.AD_REWARDED_FAILED,
                match { it["failure_stage"] == "show" }
            )
        }
        assertEquals(1, failures)
    }
}
