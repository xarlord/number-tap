package com.xarlord.numbertap.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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

// Animation constants
private const val BOUNCE_SCALE = 0.85f
private const val FADE_STEP_DURATION_MS = 120L
private const val ENTRY_INITIAL_SCALE = 0.6f
private const val TUTORIAL_TARGET_ALPHA = 0.3f
private const val TARGET_GLOW_ALPHA = 0.25f
private const val TARGET_BORDER_ALPHA = 0.4f
private const val CORNER_DECORATION_ALPHA = 0.3f
private const val CORNER_OFFSET_RATIO = 0.38f

@Composable
internal fun ThemedTile(
    tile: Tile,
    isTarget: Boolean,
    isTutorial: Boolean,
    isHardMode: Boolean,
    isHidden: Boolean,
    tileSize: Dp,
    theme: GameTheme,
    colors: ThemeColors,
    style: ThemeStyle,
    onClick: () -> Unit
) {
    // --- Phase 3.5: Smooth fade animation ---
    var fadeFrame by remember(tile.id) { mutableIntStateOf(if (tile.state != TileState.ACTIVE) 0 else -1) }

    // Scale bounce on state change
    var scale by remember(tile.id) { mutableFloatStateOf(1f) }

    LaunchedEffect(tile.state) {
        if (tile.state != TileState.ACTIVE) {
            // Bounce down then settle
            scale = BOUNCE_SCALE
            animate(
                initialValue = BOUNCE_SCALE,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            ) { value, _ ->
                scale = value
            }

            // Fade through color states
            fadeFrame = 0; delay(FADE_STEP_DURATION_MS)
            fadeFrame = 1; delay(FADE_STEP_DURATION_MS)
            fadeFrame = 2
        }
    }

    // Entry animation — subtle scale-in when tile first appears
    var entryScale by remember(tile.id) { mutableFloatStateOf(ENTRY_INITIAL_SCALE) }
    LaunchedEffect(tile.id) {
        animate(
            initialValue = ENTRY_INITIAL_SCALE,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) { value, _ ->
            entryScale = value
        }
    }

    val bg = when {
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 0 -> colors.success
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 1 -> colors.successFade
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 0 -> colors.failure
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 1 -> colors.failureFade
        isTarget && isTutorial -> colors.tileTarget.copy(alpha = TUTORIAL_TARGET_ALPHA)
        isTarget && !isHardMode -> colors.tileTargetGlow.copy(alpha = TARGET_GLOW_ALPHA)
        else -> colors.tileBackground
    }

    val cornerRadius = style.tileCornerRadius
    val shape = RoundedCornerShape(cornerRadius.dp)
    val borderModifier = if (style.showTileBorder) {
        val borderColor = when {
            isTarget && !isHardMode -> colors.tileTarget
            tile.state == TileState.TAPPED_WRONG -> colors.failure
            else -> colors.panelBorder
        }
        Modifier.border(1.dp, borderColor, shape)
    } else Modifier

    // Subtle glow for target tile (hidden in hard mode)
    val glowModifier = if (isTarget && tile.state == TileState.ACTIVE && !isHardMode) {
        Modifier.border(2.dp, colors.tileTarget.copy(alpha = TARGET_BORDER_ALPHA), shape)
    } else Modifier

    val tileDesc = if (isTarget) stringResource(R.string.a11y_tile_target, tile.currentValue)
                   else stringResource(R.string.a11y_tile, tile.currentValue)

    Box(
        modifier = Modifier
            .size(tileSize)
            .clip(shape)
            .background(bg)
            .then(borderModifier)
            .then(glowModifier)
            .graphicsLayer {
                // Apply both entry and interaction scale
                val s = entryScale * scale
                scaleX = s
                scaleY = s
            }
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
                    color = colors.textSecondary.copy(alpha = CORNER_DECORATION_ALPHA),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.offset(x = (-tileSize.value * CORNER_OFFSET_RATIO).dp, y = (-tileSize.value * CORNER_OFFSET_RATIO).dp)
                )
            }
        }

        Text(
            if (isHidden) "?" else tile.currentValue.toString(),
            color = when {
                isHidden -> colors.textSecondary.copy(alpha = 0.4f)
                tile.state == TileState.TAPPED_WRONG -> colors.failure
                isTarget && !isHardMode -> if (tile.state == TileState.ACTIVE) colors.tileTarget else colors.textPrimary
                else -> colors.textPrimary
            },
            fontSize = style.tileFontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = style.tileFontFamily
        )
    }
}
