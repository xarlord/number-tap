package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xarlord.numbertap.data.ThemeColors

@Composable
internal fun TimerBar(timeRemaining: Double, maxTime: Double, colors: ThemeColors) {
    val fraction = (timeRemaining / maxTime).coerceIn(0.0, 1.0).toFloat()
    val barColor = when {
        timeRemaining < 5 -> colors.timerUrgent
        timeRemaining < 10 -> colors.timerWarning
        else -> colors.timerSafe
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(4.dp)
            .background(colors.timerBarBg)
    ) {
        Box(modifier = Modifier.fillMaxWidth(fraction).height(4.dp).background(barColor))
    }
}
