package com.xarlord.numbertap.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.FloatingText
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    gameState: GameState,
    onTileTap: (row: Int, col: Int) -> Unit,
    onPauseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "game")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulse"
    )
    val urgentPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "urgent"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GameColors.Background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: Score | Pause | Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SCORE: %04d".format(gameState.score),
                    color = GameColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Pause button
                IconButton(onClick = onPauseClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pause",
                        tint = GameColors.TextSecondary
                    )
                }

                Text(
                    "TIME: %.1fs".format(gameState.timeRemaining),
                    color = when {
                        gameState.timeRemaining < 5 -> GameColors.TimerUrgent.copy(alpha = urgentPulse)
                        gameState.timeRemaining < 10 -> GameColors.TimerWarning
                        else -> GameColors.TextPrimary
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Timer bar
            TimerBar(timeRemaining = gameState.timeRemaining, maxTime = 30.0)

            Spacer(modifier = Modifier.height(8.dp))

            // Combo indicator
            if (gameState.comboCount > 1) {
                Text(
                    "x${gameState.comboCount} COMBO!",
                    color = GameColors.ComboGlow.copy(alpha = pulseAlpha),
                    fontSize = (18 + gameState.comboCount * 2).coerceAtMost(32).sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Target hint
            TargetHint(
                targetNumber = gameState.targetNumber,
                pulseAlpha = if (gameState.isTutorial) pulseAlpha else 1f,
                isTutorial = gameState.isTutorial
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid
            Box {
                GridContainer(
                    tiles = gameState.tiles,
                    targetNumber = gameState.targetNumber,
                    shakeOffsetPx = gameState.shakeOffset,
                    onTileTap = onTileTap,
                    isTutorial = gameState.isTutorial
                )

                // Floating texts
                FloatingTextOverlay(
                    floatingTexts = gameState.floatingTexts,
                    gridSize = gameState.gridSize
                )
            }
        }

        // Tier announcement overlay
        gameState.tierAnnouncement?.let { announcement ->
            TierAnnouncementOverlay(text = announcement)
        }

        // Tutorial overlay
        if (gameState.isTutorial) {
            TutorialOverlay(step = gameState.score)
        }

        // Pause overlay
        if (gameState.isPaused) {
            PauseOverlay(onResume = onPauseClick)
        }

        // Low time vignette
        if (gameState.timeRemaining < 5 && gameState.isPlaying && !gameState.isPaused) {
            UrgencyVignette(urgentPulse)
        }
    }
}

@Composable
private fun TimerBar(timeRemaining: Double, maxTime: Double) {
    val fraction = (timeRemaining / maxTime).coerceIn(0.0, 1.0).toFloat()
    val barColor = when {
        timeRemaining < 5 -> GameColors.TimerUrgent
        timeRemaining < 10 -> GameColors.TimerWarning
        else -> GameColors.TimerSafe
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(GameColors.TileNormal)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(barColor)
        )
    }
}

@Composable
private fun TargetHint(targetNumber: Int, pulseAlpha: Float, isTutorial: Boolean) {
    val scale = if (isTutorial) pulseAlpha else 1f
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GameColors.TileTarget.copy(alpha = scale)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            targetNumber.toString(),
            color = GameColors.Background,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
    }
    if (isTutorial) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "TAP $targetNumber!",
            color = GameColors.TileTarget,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GridContainer(
    tiles: List<List<Tile>>,
    targetNumber: Int,
    shakeOffsetPx: Pair<Float, Float>,
    onTileTap: (row: Int, col: Int) -> Unit,
    isTutorial: Boolean
) {
    val tileSize = if (isTutorial) 90.dp else if (tiles.size <= 4) 80.dp else 68.dp
    val fontSize = if (isTutorial) 28.sp else if (tiles.size <= 4) 24.sp else 20.sp

    Column(
        modifier = Modifier.offset { IntOffset(shakeOffsetPx.first.toInt(), shakeOffsetPx.second.toInt()) },
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        tiles.forEachIndexed { row, rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTiles.forEachIndexed { col, tile ->
                    TileCell(
                        tile = tile,
                        isTarget = tile.currentValue == targetNumber,
                        isTutorial = isTutorial,
                        tileSize = tileSize,
                        fontSize = fontSize,
                        onClick = { onTileTap(row, col) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TileCell(
    tile: Tile,
    isTarget: Boolean,
    isTutorial: Boolean,
    tileSize: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    var fadeFrame by remember(tile.id) { mutableIntStateOf(if (tile.state != TileState.ACTIVE) 0 else -1) }
    var bounceScale by remember(tile.id) { mutableStateOf(1f) }

    LaunchedEffect(tile.state) {
        if (tile.state != TileState.ACTIVE) {
            bounceScale = 0.9f
            fadeFrame = 0; delay(20)
            fadeFrame = 1; delay(20)
            fadeFrame = 2
            bounceScale = 1f
        }
    }

    val bg = when {
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 0 -> GameColors.Success
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 1 -> GameColors.SuccessFade
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 0 -> GameColors.Failure
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 1 -> GameColors.FailureFade
        isTarget && isTutorial -> GameColors.TileTarget.copy(alpha = 0.3f)
        isTarget -> GameColors.TileTarget.copy(alpha = 0.15f)
        else -> GameColors.TileNormal
    }

    Box(
        modifier = Modifier
            .size(tileSize)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Value range color indicator at top
        if (tile.state == TileState.ACTIVE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(GameColors.tileColorForValue(tile.currentValue))
                    .align(Alignment.TopCenter)
            )
        }
        Text(
            tile.currentValue.toString(),
            color = GameColors.TextPrimary,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FloatingTextOverlay(floatingTexts: List<FloatingText>, gridSize: Int) {
    if (floatingTexts.isEmpty()) return

    val tileSizeDp = if (gridSize <= 4) 80.dp else 68.dp
    val spacingDp = 6.dp
    val tileSizePx = with(LocalDensity.current) { tileSizeDp.toPx() }
    val spacingPx = with(LocalDensity.current) { spacingDp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        floatingTexts.forEach { ft ->
            val x = ft.x * (tileSizePx + spacingPx) + tileSizePx / 2f
            val y = ft.y * (tileSizePx + spacingPx)

            drawContext.canvas.nativeCanvas.drawText(
                ft.text,
                x,
                y,
                android.graphics.Paint().apply {
                    setColor(ft.colorHex.toInt())
                    textSize = 36f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
            )
        }
    }
}

@Composable
private fun TierAnnouncementOverlay(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "tier")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "tierScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = when (text) {
                "ROUND 2!" -> GameColors.TierMedium
                "HARD MODE!" -> GameColors.TierHard
                else -> GameColors.TileTarget
            },
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun TutorialOverlay(step: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            when {
                step < 2 -> "Tap the numbers in order!"
                step < 4 -> "Keep going!"
                else -> "Almost there!"
            },
            color = GameColors.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.PauseOverlay)
            .clickable(onClick = onResume),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PAUSED", color = GameColors.TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("TAP TO RESUME", color = GameColors.TextSecondary, fontSize = 18.sp)
        }
    }
}

@Composable
private fun UrgencyVignette(pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val vignetteWidth = width * 0.3f * pulse
        val vignetteHeight = height * 0.3f * pulse

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Red.copy(alpha = 0.3f * pulse), Color.Transparent),
                startX = 0f,
                endX = vignetteWidth
            )
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Red.copy(alpha = 0.3f * pulse)),
                startX = width - vignetteWidth,
                endX = width
            )
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Red.copy(alpha = 0.3f * pulse), Color.Transparent),
                startY = 0f,
                endY = vignetteHeight
            )
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Red.copy(alpha = 0.3f * pulse)),
                startY = height - vignetteHeight,
                endY = height
            )
        )
    }
}
