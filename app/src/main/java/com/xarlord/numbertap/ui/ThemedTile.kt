package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import kotlinx.coroutines.delay

@Composable
internal fun ThemedTile(
    tile: Tile,
    isTarget: Boolean,
    isTutorial: Boolean,
    tileSize: Dp,
    theme: GameTheme,
    colors: ThemeColors,
    style: ThemeStyle,
    onClick: () -> Unit
) {
    var fadeFrame by remember(tile.id) { mutableIntStateOf(if (tile.state != TileState.ACTIVE) 0 else -1) }

    LaunchedEffect(tile.state) {
        if (tile.state != TileState.ACTIVE) {
            fadeFrame = 0; delay(20)
            fadeFrame = 1; delay(20)
            fadeFrame = 2
        }
    }

    val bg = when {
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 0 -> colors.success
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 1 -> colors.successFade
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 0 -> colors.failure
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 1 -> colors.failureFade
        isTarget && isTutorial -> colors.tileTarget.copy(alpha = 0.3f)
        isTarget -> colors.tileTargetGlow.copy(alpha = 0.12f)
        else -> colors.tileBackground
    }

    val cornerRadius = style.tileCornerRadius
    val shape = RoundedCornerShape(cornerRadius.dp)
    val borderModifier = if (style.showTileBorder) {
        val borderColor = when {
            isTarget -> colors.tileTarget
            tile.state == TileState.TAPPED_WRONG -> colors.failure
            else -> colors.panelBorder
        }
        Modifier.border(1.dp, borderColor, shape)
    } else Modifier

    val tileDesc = if (isTarget) stringResource(R.string.a11y_tile_target, tile.currentValue)
                   else stringResource(R.string.a11y_tile, tile.currentValue)

    Box(
        modifier = Modifier
            .size(tileSize)
            .clip(shape)
            .background(bg)
            .then(borderModifier)
            .semantics {
                contentDescription = tileDesc
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // ASCII-style corner decoration for Terminal/Matrix
        if (theme == GameTheme.TERMINAL || theme == GameTheme.MATRIX) {
            val cornerChar = style.tileCornerChar
            if (cornerChar.isNotEmpty() && tile.state == TileState.ACTIVE) {
                Text(
                    cornerChar,
                    color = colors.textSecondary.copy(alpha = 0.3f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.offset(x = (-tileSize.value * 0.38).dp, y = (-tileSize.value * 0.38).dp)
                )
            }
        }

        Text(
            tile.currentValue.toString(),
            color = when {
                tile.state == TileState.TAPPED_WRONG -> colors.failure
                isTarget -> if (tile.state == TileState.ACTIVE) colors.tileTarget else colors.textPrimary
                else -> colors.textPrimary
            },
            fontSize = style.tileFontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = style.tileFontFamily
        )
    }
}
