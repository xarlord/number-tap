package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle

@Composable
internal fun TargetHint(
    targetNumber: Int,
    colors: ThemeColors,
    style: ThemeStyle,
    pulseAlpha: Float,
    isTutorial: Boolean
) {
    val cornerRadius = style.tileCornerRadius
    val bgColor = colors.tileTarget.copy(alpha = if (isTutorial) pulseAlpha else 1f)
    val shape = RoundedCornerShape(cornerRadius.dp)
    val findDesc = stringResource(R.string.a11y_find_number, targetNumber)

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(shape)
            .background(bgColor)
            .then(if (style.showTileBorder) Modifier.border(2.dp, colors.tileTarget, shape) else Modifier)
            .semantics { contentDescription = findDesc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            targetNumber.toString(),
            color = colors.textTarget,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.tileFontFamily
        )
    }
    if (isTutorial) {
        Text(
            stringResource(R.string.target_hint, targetNumber),
            color = colors.tileTarget,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.bodyFontFamily
        )
    }
}
