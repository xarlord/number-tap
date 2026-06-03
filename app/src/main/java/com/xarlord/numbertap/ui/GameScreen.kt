package com.xarlord.numbertap.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.Tile
import com.xarlord.numbertap.data.TileState

@Composable
fun GameScreen(
    gameState: GameState,
    onTileTap: (row: Int, col: Int) -> Unit,
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

        // Grid
        GridContainer(
            tiles = gameState.tiles,
            targetNumber = gameState.targetNumber,
            shakeOffset = gameState.shakeOffset,
            onTileTap = onTileTap
        )
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
    shakeOffset: Pair<Float, Float>,
    onTileTap: (row: Int, col: Int) -> Unit
) {
    Column(
        modifier = Modifier.offset(
            x = shakeOffset.first.dp,
            y = shakeOffset.second.dp
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

@Composable
private fun TileCell(
    tile: Tile,
    isTarget: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (tile.state) {
        TileState.TAPPED_CORRECT -> GameColors.Success
        TileState.TAPPED_WRONG -> GameColors.Failure
        else -> if (isTarget) GameColors.TileTarget.copy(alpha = 0.15f) else GameColors.TileNormal
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
