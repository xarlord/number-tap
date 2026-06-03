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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    highScore: Int,
    onStartClick: () -> Unit,
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
            text = "🔢",
            fontSize = 72.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NUMBER TAP",
            color = GameColors.TextPrimary,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The Ordered Grid",
            color = GameColors.TextSecondary,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (highScore > 0) {
            Text(
                text = "Best: $highScore",
                color = GameColors.TileTarget,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = GameColors.TileTarget,
                contentColor = Color(0xFF121824)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(width = 180.dp, height = 56.dp)
        ) {
            Text("START", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
