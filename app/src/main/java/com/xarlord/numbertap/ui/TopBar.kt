package com.xarlord.numbertap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle

@Composable
internal fun TopBar(
    state: GameState,
    colors: ThemeColors,
    style: ThemeStyle,
    onPauseClick: () -> Unit,
    urgentPulse: Float
) {
    val scoreDesc = stringResource(R.string.a11y_score, state.score)
    val timeDesc = stringResource(R.string.a11y_time, state.timeRemaining)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.score_format, state.score),
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.headerFontFamily,
            modifier = Modifier.semantics { contentDescription = scoreDesc }
        )
        IconButton(onClick = onPauseClick, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pause),
                contentDescription = stringResource(R.string.pause_desc),
                tint = colors.textSecondary
            )
        }
        Text(
            stringResource(R.string.time_format, state.timeRemaining),
            color = when {
                state.timeRemaining < 5 -> colors.timerUrgent.copy(alpha = urgentPulse)
                state.timeRemaining < 10 -> colors.timerWarning
                else -> colors.textPrimary
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.headerFontFamily,
            modifier = Modifier.semantics { contentDescription = timeDesc }
        )
    }
}
