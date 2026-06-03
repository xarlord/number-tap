package com.xarlord.numbertap.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    gameState: GameState,
    onTileTap: (row: Int, col: Int) -> Unit,
    onFeedbackComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameColors.Background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top stats bar
        StatsBar(score = gameState.score, timeRemaining = gameState.timeRemaining)

        Spacer(modifier = Modifier.height(24.dp))

        // Target number hint
        TargetHint(targetNumber = gameState.targetNumber)

        Spacer(modifier = Modifier.height(24.dp))

        // Grid with shake in pixels
        val density = LocalDensity.current
        GridContainer(
            tiles = gameState.tiles,
            targetNumber = gameState.targetNumber,
            shakeOffsetPx = gameState.shakeOffset,
            onTileTap = onTileTap,
            density = density
        )
    }

    // #38/#43: Delay feedback clear so user sees the flash (100ms = ~6 frames)
    val hasFeedback = gameState.tiles.any { row -> row.any { it.state != TileState.ACTIVE } }
    LaunchedEffect(hasFeedback) {
        if (hasFeedback) {
            delay(100) // Visible shake + color flash
            onFeedbackComplete()
        }
    }
}

@Composable
private fun StatsBar(score: Int, timeRemaining: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "SCORE: %04d".format(score),
            color = GameColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "TIME: %.1fs".format(timeRemaining),
            color = if (timeRemaining < 5) GameColors.Failure else GameColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TargetHint(targetNumber: Int) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GameColors.TileTarget),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = targetNumber.toString(),
            color = Color(0xFF121824),
            fontSize = 36.sp,
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
    density: androidx.compose.ui.unit.Density
) {
    Column(
        modifier = Modifier.offset(
            x = with(density) { shakeOffsetPx.first.toInt().toDp() },
            y = with(density) { shakeOffsetPx.second.toInt().toDp() }
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        tiles.forEachIndexed { row, rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTiles.forEachIndexed { col, tile ->
                    TileCell(
                        tile = tile,
                        isTarget = tile.currentValue == targetNumber,
                        onClick = { onTileTap(row, col) }
                    )
                }
            }
        }
    }
}

/**
 * #43/#44: Proper 3-frame color fade implementation per GDD Section 4.1.
 * Frame 1 (Impact): Pure green/red → Frame 2 (Fade): Muted green/red → Frame 3 (Settle): Normal
 * The frame transitions are driven by a timed state machine.
 */
@Composable
private fun TileCell(
    tile: Tile,
    isTarget: Boolean,
    onClick: () -> Unit
) {
    // 3-frame color fade state machine
    var fadeFrame by remember(tile.id) { mutableStateOf(if (tile.state != TileState.ACTIVE) 0 else -1) }

    // Track state changes to trigger fade
    LaunchedEffect(tile.state) {
        if (tile.state != TileState.ACTIVE) {
            fadeFrame = 0 // Start at impact frame
            delay(32)     // ~2 frames at 60Hz
            fadeFrame = 1 // Fade frame
            delay(32)
            fadeFrame = 2 // Settle frame (resets to normal via onFeedbackComplete)
        }
    }

    val backgroundColor = when {
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 0 -> GameColors.Success       // Frame 1: #22C55E
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 1 -> GameColors.SuccessFade    // Frame 2: #1E5E3A
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 0 -> GameColors.Failure          // Frame 1: #EF4444
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 1 -> GameColors.FailureFade      // Frame 2: #6B2121
        isTarget -> GameColors.TileTarget.copy(alpha = 0.15f)
        else -> GameColors.TileNormal                                                           // Frame 3: #2A3447
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.currentValue.toString(),
            color = GameColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
