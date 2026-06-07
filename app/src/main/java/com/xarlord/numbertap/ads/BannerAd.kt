package com.xarlord.numbertap.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Composable banner ad for AdMob.
 * Issue #90: Banner ad placed below game content, clear of gameplay grid.
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent)
            .padding(horizontal = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = AdManagerImpl.BANNER_AD_UNIT_ID
                    setAdSize(AdSize.BANNER)
                    loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                }
            }
        )
    }
}
