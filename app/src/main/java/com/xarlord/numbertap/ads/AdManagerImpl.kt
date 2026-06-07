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
 */
class AdManagerImpl(
    private val context: Context
) : AdManager {

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

    // Callbacks for rewarded ad
    private var onRewardEarned: (() -> Unit)? = null
    private var onRewardFailed: (() -> Unit)? = null

    /**
     * Initialize the Mobile Ads SDK. Call once in Application.onCreate() or Activity.onCreate().
     */
    fun initialize() {
        if (isInitialized) return
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob SDK initialized")
            isInitialized = true
        }
    }

    override fun loadBanner(): Boolean {
        // Banner is loaded inline in Compose via AndroidView + AdView
        // This method confirms the SDK is initialized
        return isInitialized
    }

    /**
     * Preload an interstitial ad for later display.
     */
    fun preloadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
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

    override fun showInterstitial(): Boolean {
        gameOverCount++
        if (gameOverCount % INTERSTITIAL_FREQUENCY != 0) {
            Log.d(TAG, "Skipping interstitial (game over #$gameOverCount)")
            return false
        }
        val ad = interstitialAd
        if (ad != null && context is Activity) {
            ad.show(context)
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
            context,
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
                            onRewardFailed?.invoke()
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
        val ad = rewardedAd
        if (ad != null && context is Activity) {
            ad.show(context) { rewardItem ->
                val amount = rewardItem.amount
                val type = rewardItem.type
                Log.d(TAG, "Reward earned: $amount $type")
                onRewardEarned?.invoke()
            }
            Log.d(TAG, "Showing rewarded ad")
            return true
        }
        Log.d(TAG, "No rewarded ad ready")
        onRewardFailed?.invoke()
        return false
    }

    override fun isAdReady(): Boolean = rewardedAd != null

    /**
     * Show rewarded ad with callbacks for success/failure.
     * Issue #16: Used for the +5s revive mechanic.
     */
    fun showRewardedWithCallbacks(
        onReward: () -> Unit,
        onFailure: () -> Unit
    ) {
        onRewardEarned = onReward
        onRewardFailed = onFailure
        if (!showRewardedAd()) {
            onFailure()
        }
    }

    /**
     * Add test device IDs for development.
     * Call after initialize() with your device ID from logcat.
     */
    fun addTestDeviceIds(deviceIds: List<String>) {
        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(deviceIds)
            .build()
        MobileAds.setRequestConfiguration(config)
    }
}
