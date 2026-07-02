package com.xarlord.numbertap.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Real AdMob implementation — banner, interstitial, rewarded ads.
 * Issue #90: Full AdMob SDK integration.
 *
 * AdMob App ID: ca-app-pub-2335615408331368~9734224673
 *
 * Real Ad Unit IDs from AdMob console:
 * - Banner (menu_banner): ca-app-pub-2335615408331368/9191829422
 * - Interstitial (game_over_interstitial): ca-app-pub-2335615408331368/3129287879
 * - Rewarded (revive_rewarded): ca-app-pub-2335615408331368/8440334628
 *
 * #138 fix: Stores applicationContext to avoid Activity leaks.
 * Accepts Activity parameter at show-time for full-screen ads.
 */
class AdManagerImpl(
    context: Context
) : AdManager {

    private val appContext: Context = context.applicationContext

    companion object {
        private const val TAG = "NumberTap:Ads"

        const val BANNER_AD_UNIT_ID = "ca-app-pub-2335615408331368/9191829422"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2335615408331368/3129287879"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-2335615408331368/8440334628"

        // Show interstitial every N game overs
        const val INTERSTITIAL_FREQUENCY = 3
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var gameOverCount = 0
    private var isInitialized = false
    private var initInProgress = false

    /**
     * Initialize the Mobile Ads SDK. Call once in Application.onCreate() or Activity.onCreate().
     */
    fun initialize() {
        if (isInitialized || initInProgress) return
        initInProgress = true
        MobileAds.initialize(appContext) {
            Log.d(TAG, "AdMob SDK initialized")
            isInitialized = true
            initInProgress = false
            // #142: Auto-register emulator as test device in debug builds
            if (com.xarlord.numbertap.BuildConfig.DEBUG) {
                val config = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(config)
                Log.d(TAG, "Debug build: registered emulator test device")
            }
        }
    }

    override fun loadBanner(): Boolean {
        // Banner is loaded inline in Compose via AndroidView + AdView
        // This method confirms the SDK is initialized or initialization is in progress
        return isInitialized || initInProgress
    }

    /**
     * Preload an interstitial ad for later display.
     */
    fun preloadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            preloadInterstitial() // Preload next
                            Log.d(TAG, "Interstitial dismissed")
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialAd = null
                            preloadInterstitial()
                            Log.w(TAG, "Interstitial show failed: ${error.message}")
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial load failed: ${error.message}")
                }
            }
        )
    }

    /**
     * Show interstitial ad. Requires an Activity for full-screen display.
     * #138 fix: Activity passed at show-time, not stored.
     */
    override fun showInterstitial(): Boolean {
        // #144 fix: Do NOT increment gameOverCount — the Activity overload handles counting.
        // This no-arg stub is kept for interface compliance but should not affect state.
        Log.d(TAG, "No Activity provided — use showInterstitial(activity) for full-screen ads")
        return false
    }

    /**
     * Show interstitial with an Activity context. Use this from game-over screen.
     */
    fun showInterstitial(activity: Activity): Boolean {
        gameOverCount++
        if (gameOverCount % INTERSTITIAL_FREQUENCY != 0) {
            Log.d(TAG, "Skipping interstitial (game over #$gameOverCount)")
            return false
        }
        val ad = interstitialAd
        if (ad != null) {
            ad.show(activity)
            Log.d(TAG, "Showing interstitial ad")
            return true
        }
        Log.d(TAG, "No interstitial ad ready")
        return false
    }

    /**
     * Preload a rewarded ad for later display.
     */
    fun preloadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad loaded")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            preloadRewarded() // Preload next
                            Log.d(TAG, "Rewarded ad dismissed")
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            rewardedAd = null
                            preloadRewarded()
                            Log.w(TAG, "Rewarded show failed: ${error.message}")
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.w(TAG, "Rewarded load failed: ${error.message}")
                }
            }
        )
    }

    override fun showRewardedAd(): Boolean {
        Log.d(TAG, "Use showRewardedAd(activity) overload for proper Activity context")
        return false
    }

    /**
     * Show rewarded ad with Activity context — DISPLAY ONLY, does NOT grant rewards.
     * #138 fix: Activity passed at show-time, not stored.
     * #151 fix: Must return true when ad IS available — onReward is async and won't fire
     * before this method returns, so we cannot capture its result synchronously.
     *
     * #153: The reward callback is a no-op (only logs). Callers that need to know
     * the reward outcome (e.g., +5s revive mechanic) MUST use [showRewardedWithCallbacks]
     * instead. This method is unsuitable for game mechanics.
     *
     * @param activity Activity context for full-screen ad display
     * @return true if ad was shown, false if no ad ready
     */
    @Deprecated(
        message = "Use showRewardedWithCallbacks(activity, onReward, onFailure) to handle reward outcomes. " +
            "This method silently discards the reward (issue #153).",
        replaceWith = ReplaceWith("showRewardedWithCallbacks(activity, onReward = {}, onFailure = {})")
    )
    fun showRewardedAd(activity: Activity): Boolean {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
            }
            Log.d(TAG, "Showing rewarded ad")
            return true
        }
        Log.d(TAG, "No rewarded ad ready")
        return false
    }

    override fun isAdReady(): Boolean = rewardedAd != null

    /**
     * Show rewarded ad with callbacks for success/failure.
     * Issue #16: Used for the +5s revive mechanic.
     * Note: showRewardedAd(activity) does NOT delegate here — it has its own
     * ad.show() call. Use THIS method when you need to know the reward outcome.
     */
    fun showRewardedWithCallbacks(
        activity: Activity,
        onReward: () -> Unit,
        onFailure: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                onReward()
            }
            Log.d(TAG, "Showing rewarded ad")
        } else {
            Log.d(TAG, "No rewarded ad ready for revive")
            onFailure()
        }
    }

    /**
     * Add test device IDs for development.
     */
    fun addTestDeviceIds(deviceIds: List<String>) {
        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(deviceIds)
            .build()
        MobileAds.setRequestConfiguration(config)
    }
}
