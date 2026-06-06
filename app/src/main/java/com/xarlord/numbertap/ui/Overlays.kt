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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle

@Composable
internal fun ScanlineOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = 3.dp.toPx()
        val gap = 2.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawRect(
                color = Color.Black.copy(alpha = 0.08f),
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
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onResume),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.paused), color = colors.textPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.tap_to_resume), color = colors.textSecondary, fontSize = 16.sp, fontFamily = style.bodyFontFamily)
        }
    }
}

@Composable
internal fun UrgencyVignette(vignetteColor: Color, pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val vw = w * 0.25f * pulse
        val vh = h * 0.25f * pulse
        drawRect(Brush.horizontalGradient(listOf(vignetteColor.copy(alpha = 0.25f * pulse), Color.Transparent), 0f, vw))
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, vignetteColor.copy(alpha = 0.25f * pulse)), w - vw, w))
        drawRect(Brush.verticalGradient(listOf(vignetteColor.copy(alpha = 0.25f * pulse), Color.Transparent), 0f, vh))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, vignetteColor.copy(alpha = 0.25f * pulse)), h - vh, h))
    }
}
