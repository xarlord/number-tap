package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xarlord.numbertap.ui.GameColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameColors.Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GAME OVER",
            color = GameColors.Failure,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Score: $score",
            color = GameColors.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Best: $highScore",
            color = GameColors.TileTarget,
            fontSize = 24.sp
        )
        if (score >= highScore && score > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🏆 NEW BEST!",
                color = GameColors.TileTarget,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onPlayAgain,
            colors = ButtonDefaults.buttonColors(
                containerColor = GameColors.TileTarget,
                contentColor = GameColors.Background
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(width = 220.dp, height = 56.dp)
        ) {
            Text("PLAY AGAIN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onMenu,
            colors = ButtonDefaults.buttonColors(
                containerColor = GameColors.TileNormal,
                contentColor = GameColors.TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(width = 220.dp, height = 48.dp)
        ) {
            Text("MENU", fontSize = 16.sp)
        }
    }
}
