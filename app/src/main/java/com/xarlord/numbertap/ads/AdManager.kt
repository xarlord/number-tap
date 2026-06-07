package com.xarlord.numbertap.ads

/**
 * AdMob ad management interface.
 * Issue #15: AdMob SDK scaffolding — stub implementation ready for real integration.
 */
interface AdManager {
    /** Load a banner ad. Returns false in stub. */
    fun loadBanner(): Boolean

    /** Show an interstitial ad. Returns false in stub. */
    fun showInterstitial(): Boolean

    /** Show a rewarded ad. Returns false in stub. */
    fun showRewardedAd(): Boolean

    /** Check if a rewarded ad is ready to show. */
    fun isAdReady(): Boolean
}

/**
 * Stub implementation — all methods return false / no-ops.
 * Replace with real AdManagerImpl when AdMob is configured.
 */
class StubAdManager : AdManager {
    override fun loadBanner(): Boolean = false
    override fun showInterstitial(): Boolean = false
    override fun showRewardedAd(): Boolean = false
    override fun isAdReady(): Boolean = false
}
