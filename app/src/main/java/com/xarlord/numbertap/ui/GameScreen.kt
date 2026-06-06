package com.xarlord.numbertap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import com.xarlord.numbertap.data.ThemeConfig
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    gameState: GameState,
    onTileTap: (row: Int, col: Int) -> Unit,
    onPauseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = gameState.currentTheme
    val colors = ThemeConfig.colorsFor(theme)
    val style = ThemeConfig.styleFor(theme)

    val infiniteTransition = rememberInfiniteTransition(label = "game")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "pulse"
    )
    val urgentPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "urgent"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // === TOP: Stats Bar (fixed height) ===
            TopBar(gameState, colors, style, onPauseClick, urgentPulse)

            // Timer bar
            TimerBar(gameState.timeRemaining, 30.0, colors)

            // === CENTER: Game Grid (takes remaining space) ===
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Target hint
                    TargetHint(
                        targetNumber = gameState.targetNumber,
                        colors = colors,
                        style = style,
                        pulseAlpha = if (gameState.isTutorial) pulseAlpha else 1f,
                        isTutorial = gameState.isTutorial
                    )

                    if (gameState.comboCount > 1) {
                        Text(
                            "x${gameState.comboCount} COMBO!",
                            color = colors.comboGlow.copy(alpha = pulseAlpha),
                            fontSize = (16 + gameState.comboCount * 2).coerceAtMost(28).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = style.headerFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid
                    GridContainer(
                        tiles = gameState.tiles,
                        targetNumber = gameState.targetNumber,
                        shakeOffsetPx = gameState.shakeOffset,
                        onTileTap = onTileTap,
                        isTutorial = gameState.isTutorial,
                        theme = theme,
                        colors = colors,
                        style = style
                    )
                }

                // Tier announcement overlay
                gameState.tierAnnouncement?.let { ann ->
                    Text(
                        ann,
                        color = when (ann) {
                            "ROUND 2!" -> colors.timerWarning
                            "HARD MODE!" -> colors.timerUrgent
                            else -> colors.tileTarget
                        },
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = style.headerFontFamily,
                        textAlign = TextAlign.Center
                    )
                }

                // Tutorial overlay
                if (gameState.isTutorial) {
                    Text(
                        when {
                            gameState.score < 2 -> "Tap the numbers in order!"
                            gameState.score < 4 -> "Keep going!"
                            else -> "Almost there!"
                        },
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        fontFamily = style.bodyFontFamily,
                        modifier = Modifier.offset(y = (-120).dp)
                    )
                }
            }

            // === BOTTOM: Stats Panel (fixed height) ===
            BottomPanel(gameState, colors, style)
        }

        // Scanline overlay for Terminal theme
        if (style.showScanlines) {
            ScanlineOverlay()
        }

        // Pause overlay
        if (gameState.isPaused) {
            PauseOverlay(colors, style, onResume = onPauseClick)
        }

        // Urgency vignette
        if (gameState.timeRemaining < 5 && gameState.isPlaying && !gameState.isPaused) {
            UrgencyVignette(colors.vignetteColor, urgentPulse)
        }
    }
}

@Composable
private fun TopBar(
    state: GameState,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle,
    onPauseClick: () -> Unit,
    urgentPulse: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "SCORE: %04d".format(state.score),
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.headerFontFamily
        )
        IconButton(onClick = onPauseClick, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pause),
                contentDescription = "Pause",
                tint = colors.textSecondary
            )
        }
        Text(
            "TIME: %.1fs".format(state.timeRemaining),
            color = when {
                state.timeRemaining < 5 -> colors.timerUrgent.copy(alpha = urgentPulse)
                state.timeRemaining < 10 -> colors.timerWarning
                else -> colors.textPrimary
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.headerFontFamily
        )
    }
}

@Composable
private fun TimerBar(timeRemaining: Double, maxTime: Double, colors: com.xarlord.numbertap.data.ThemeColors) {
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

@Composable
private fun TargetHint(
    targetNumber: Int,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle,
    pulseAlpha: Float,
    isTutorial: Boolean
) {
    val cornerRadius = style.tileCornerRadius
    val bgColor = colors.tileTarget.copy(alpha = if (isTutorial) pulseAlpha else 1f)
    val shape = RoundedCornerShape(cornerRadius.dp)

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(shape)
            .background(bgColor)
            .then(if (style.showTileBorder) Modifier.border(2.dp, colors.tileTarget, shape) else Modifier),
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
            "TAP $targetNumber!",
            color = colors.tileTarget,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = style.bodyFontFamily
        )
    }
}

@Composable
private fun GridContainer(
    tiles: List<List<Tile>>,
    targetNumber: Int,
    shakeOffsetPx: Pair<Float, Float>,
    onTileTap: (row: Int, col: Int) -> Unit,
    isTutorial: Boolean,
    theme: GameTheme,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle
) {
    val tileSize = if (isTutorial) 88.dp else if (tiles.size <= 4) 78.dp else 64.dp

    Column(
        modifier = Modifier.offset { IntOffset(shakeOffsetPx.first.toInt(), shakeOffsetPx.second.toInt()) },
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        tiles.forEachIndexed { row, rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTiles.forEachIndexed { col, tile ->
                    ThemedTile(
                        tile = tile,
                        isTarget = tile.currentValue == targetNumber,
                        isTutorial = isTutorial,
                        tileSize = tileSize,
                        theme = theme,
                        colors = colors,
                        style = style,
                        onClick = { onTileTap(row, col) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemedTile(
    tile: Tile,
    isTarget: Boolean,
    isTutorial: Boolean,
    tileSize: androidx.compose.ui.unit.Dp,
    theme: GameTheme,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle,
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

    Box(
        modifier = Modifier
            .size(tileSize)
            .clip(shape)
            .background(bg)
            .then(borderModifier)
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

@Composable
private fun BottomPanel(
    state: GameState,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle
) {
    val tier = when {
        state.score <= 15 -> "EASY"
        state.score <= 40 -> "MEDIUM"
        else -> "HARD"
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
                    "EASY" -> colors.timerSafe
                    "MEDIUM" -> colors.timerWarning
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
                Text("MAX", color = colors.timerUrgent, fontSize = 10.sp, fontFamily = style.bodyFontFamily)
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
            StatLabel("ACC", "${accPct}%", colors, style)
            StatLabel("AVG", if (avgMs > 0) "${(avgMs / 1000).toInt()}s" else "—", colors, style)
            StatLabel("BEST", "x${state.maxCombo}", colors, style)
            StatLabel("TAPS", "${state.totalTaps}", colors, style)
        }
    }
}

@Composable
private fun StatLabel(
    label: String,
    value: String,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = colors.textSecondary, fontSize = 9.sp, fontFamily = style.bodyFontFamily)
        Text(value, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = style.tileFontFamily)
    }
}

@Composable
private fun ScanlineOverlay() {
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
private fun PauseOverlay(
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle,
    onResume: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onResume),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PAUSED", color = colors.textPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily)
            Spacer(modifier = Modifier.height(16.dp))
            Text("TAP TO RESUME", color = colors.textSecondary, fontSize = 16.sp, fontFamily = style.bodyFontFamily)
        }
    }
}

@Composable
private fun UrgencyVignette(vignetteColor: Color, pulse: Float) {
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
