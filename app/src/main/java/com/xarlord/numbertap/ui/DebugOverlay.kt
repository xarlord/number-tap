package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.data.DifficultyConfig
import com.xarlord.numbertap.data.GameState

/**
 * Debug overlay — shows tuning parameters and live game telemetry.
 * Only rendered when BuildConfig.DEBUG && debugOverlayEnabled.
 * Issue #100: In-game tuning data visualization.
 */
@Composable
fun DebugOverlay(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val tier = DifficultyConfig.tierForScore(gameState.score)
    val accuracy = if (gameState.totalTaps > 0) {
        (gameState.correctTaps * 100.0 / gameState.totalTaps)
    } else 0.0
    val avgTapTime = if (gameState.correctTaps > 0) {
        gameState.totalTapTimeNs / gameState.correctTaps / 1_000_000.0
    } else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC000000))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = "─── DEBUG ───",
            color = Color(0xFF00FF00),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Tier: ${tier.label} (${tier.gridRows}×${tier.gridCols}) | +${tier.timeGainSeconds}s / -${tier.timePenaltySeconds}s",
            color = Color(0xFF00FF00),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Score: ${gameState.score} | Target: ${gameState.targetNumber} | Combo: ${gameState.comboCount} | MaxCombo: ${gameState.maxCombo}",
            color = Color(0xFF00FF00),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "ACC: ${"%.1f".format(accuracy)}% | AVG: ${"%.0f".format(avgTapTime)}ms | Taps: ${gameState.correctTaps}C/${gameState.wrongTaps}W",
            color = Color(0xFF00FF00),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Time: ${"%.2f".format(gameState.timeRemaining)}s | Grid: ${gameState.tiles.size} tiles",
            color = Color(0xFF00FF00),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
