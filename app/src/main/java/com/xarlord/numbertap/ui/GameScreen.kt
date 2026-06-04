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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(GameColors.Background).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatsBar(score = gameState.score, timeRemaining = gameState.timeRemaining)
        Spacer(modifier = Modifier.height(24.dp))
        TargetHint(targetNumber = gameState.targetNumber)
        Spacer(modifier = Modifier.height(24.dp))
        GridContainer(
            tiles = gameState.tiles,
            targetNumber = gameState.targetNumber,
            shakeOffsetPx = gameState.shakeOffset,
            onTileTap = onTileTap
        )
    }
}

@Composable
private fun StatsBar(score: Int, timeRemaining: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("SCORE: %04d".format(score), color = GameColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("TIME: %.1fs".format(timeRemaining), color = if (timeRemaining < 5) GameColors.Failure else GameColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TargetHint(targetNumber: Int) {
    Box(
        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(GameColors.TileTarget),
        contentAlignment = Alignment.Center
    ) {
        // #60: Use GameColors constant instead of hardcoded Color(0xFF121824)
        Text(targetNumber.toString(), color = GameColors.Background, fontSize = 36.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GridContainer(
    tiles: List<List<Tile>>,
    targetNumber: Int,
    shakeOffsetPx: Pair<Float, Float>,
    onTileTap: (row: Int, col: Int) -> Unit
) {
    // #56: GDD specifies pixel offsets — IntOffset for raw px, no dp conversion
    Column(
        modifier = Modifier.offset { IntOffset(shakeOffsetPx.first.toInt(), shakeOffsetPx.second.toInt()) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        tiles.forEachIndexed { row, rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTiles.forEachIndexed { col, tile ->
                    TileCell(tile = tile, isTarget = tile.currentValue == targetNumber, onClick = { onTileTap(row, col) })
                }
            }
        }
    }
}

/** #43: 3-frame color fade per GDD §4.1: Impact → Fade → Settle */
@Composable
private fun TileCell(tile: Tile, isTarget: Boolean, onClick: () -> Unit) {
    var fadeFrame by remember(tile.id) { mutableIntStateOf(if (tile.state != TileState.ACTIVE) 0 else -1) }

    LaunchedEffect(tile.state) {
        if (tile.state != TileState.ACTIVE) {
            fadeFrame = 0; delay(20)  // Frame 1: Impact
            fadeFrame = 1; delay(20)  // Frame 2: Fade
            fadeFrame = 2             // Frame 3: Settle
        }
    }

    val bg = when {
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 0 -> GameColors.Success
        tile.state == TileState.TAPPED_CORRECT && fadeFrame == 1 -> GameColors.SuccessFade
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 0 -> GameColors.Failure
        tile.state == TileState.TAPPED_WRONG && fadeFrame == 1 -> GameColors.FailureFade
        isTarget -> GameColors.TileTarget.copy(alpha = 0.15f)
        else -> GameColors.TileNormal
    }

    Box(
        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(tile.currentValue.toString(), color = GameColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
