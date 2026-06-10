package com.xarlord.numbertap.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle

// Scanline overlay constants
private const val SCANLINE_ALPHA = 0.08f
private const val SCANLINE_HEIGHT_DP = 3
private const val SCANLINE_GAP_DP = 2

// Pause overlay constants
private const val PAUSE_DIM_ALPHA = 0.7f
private const val PAUSE_TITLE_FONT_SIZE_SP = 36
private const val PAUSE_SUBTITLE_FONT_SIZE_SP = 16
private const val PAUSE_SPACING_DP = 16

// Urgency vignette constants
private const val VIGNette_WIDTH_RATIO = 0.25f
private const val VIGNETTE_ALPHA_BASE = 0.25f

@Composable
internal fun ScanlineOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = SCANLINE_HEIGHT_DP.dp.toPx()
        val gap = SCANLINE_GAP_DP.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawRect(
                color = Color.Black.copy(alpha = SCANLINE_ALPHA),
                topLeft = Offset(0f, y),
                size = Size(size.width, lineHeight)
            )
            y += lineHeight + gap
        }
    }
}

@Composable
internal fun PauseOverlay(
    colors: ThemeColors,
    style: ThemeStyle,
    onResume: () -> Unit
) {
    val pausedDesc = stringResource(R.string.a11y_paused)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = PAUSE_DIM_ALPHA))
            .semantics {
                contentDescription = pausedDesc
                role = Role.Button
            }
            .clickable(onClick = onResume),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.paused), color = colors.textPrimary, fontSize = PAUSE_TITLE_FONT_SIZE_SP.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily)
            Spacer(modifier = Modifier.height(PAUSE_SPACING_DP.dp))
            Text(stringResource(R.string.tap_to_resume), color = colors.textSecondary, fontSize = PAUSE_SUBTITLE_FONT_SIZE_SP.sp, fontFamily = style.bodyFontFamily)
        }
    }
}

@Composable
internal fun UrgencyVignette(vignetteColor: Color, pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val vw = w * VIGNette_WIDTH_RATIO * pulse
        val vh = h * VIGNette_WIDTH_RATIO * pulse
        drawRect(Brush.horizontalGradient(listOf(vignetteColor.copy(alpha = VIGNETTE_ALPHA_BASE * pulse), Color.Transparent), 0f, vw))
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, vignetteColor.copy(alpha = VIGNETTE_ALPHA_BASE * pulse)), w - vw, w))
        drawRect(Brush.verticalGradient(listOf(vignetteColor.copy(alpha = VIGNETTE_ALPHA_BASE * pulse), Color.Transparent), 0f, vh))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, vignetteColor.copy(alpha = VIGNETTE_ALPHA_BASE * pulse)), h - vh, h))
    }
}
