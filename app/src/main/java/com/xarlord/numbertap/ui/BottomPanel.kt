package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle

@Composable
internal fun BottomPanel(
    state: GameState,
    colors: ThemeColors,
    style: ThemeStyle
) {
    val strEasy = stringResource(R.string.easy)
    val strMedium = stringResource(R.string.medium)
    val strHard = stringResource(R.string.hard)
    val strMax = stringResource(R.string.max)
    val strAcc = stringResource(R.string.stat_acc)
    val strAvg = stringResource(R.string.stat_avg)
    val strBest = stringResource(R.string.stat_best)
    val strTaps = stringResource(R.string.stat_taps)

    val tier = when {
        state.score <= 15 -> strEasy
        state.score <= 40 -> strMedium
        else -> strHard
    }
    val nextTierAt = when {
        state.score <= 15 -> 16
        state.score <= 40 -> 41
        else -> null
    }
    val tierProgress = if (nextTierAt != null) {
        when {
            state.score <= 15 -> state.score.toFloat() / 15
            state.score <= 40 -> (state.score - 15).toFloat() / 25
            else -> 1f
        }
    } else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panelBackground)
            .border(width = 1.dp, color = colors.panelBorder.copy(alpha = 0.3f), shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Tier progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                tier,
                color = when (tier) {
                    strEasy -> colors.timerSafe
                    strMedium -> colors.timerWarning
                    else -> colors.timerUrgent
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.bodyFontFamily
            )
            if (nextTierAt != null) {
                // Progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(horizontal = 8.dp)
                        .background(colors.timerBarBg)
                        .clip(RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(tierProgress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(colors.textSecondary.copy(alpha = 0.5f))
                    )
                }
                Text(
                    "$nextTierAt",
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = style.bodyFontFamily
                )
            } else {
                Text(strMax, color = colors.timerUrgent, fontSize = 10.sp, fontFamily = style.bodyFontFamily)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val accPct = if (state.totalTaps > 0) (state.correctTaps * 100 / state.totalTaps) else 0
            val avgMs = state.avgTapTimeMs
            StatLabel(strAcc, "${accPct}%", colors, style)
            StatLabel(strAvg, if (avgMs > 0) "${(avgMs / 1000).toInt()}s" else "—", colors, style)
            StatLabel(strBest, "x${state.maxCombo}", colors, style)
            StatLabel(strTaps, "${state.totalTaps}", colors, style)
        }
    }
}

@Composable
internal fun StatLabel(
    label: String,
    value: String,
    colors: ThemeColors,
    style: ThemeStyle
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = colors.textSecondary, fontSize = 9.sp, fontFamily = style.bodyFontFamily)
        Text(value, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = style.tileFontFamily)
    }
}
