package com.xarlord.numbertap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    isReviveEligible: Boolean,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    onShare: () -> Unit = {},
    onRevive: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animated score counter
    var displayScore by remember { mutableStateOf(0) }
    LaunchedEffect(score) {
        val steps = 30
        val increment = (score.toFloat() / steps).coerceAtLeast(1f).toInt()
        while (displayScore < score) {
            displayScore = (displayScore + increment).coerceAtMost(score)
            kotlinx.coroutines.delay(30)
        }
        displayScore = score
    }

    val isNewBest = isNewHighScore && score > 0

    Box(modifier = modifier.fillMaxSize()) {
        // Confetti for new high score
        if (isNewBest) {
            ConfettiAnimation()
        }

        Column(
            modifier = Modifier
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

            // Animated score
            Text(
                text = "$displayScore",
                color = GameColors.TextPrimary,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BEST: $highScore",
                color = GameColors.TileTarget,
                fontSize = 24.sp
            )

            // New best celebration
            if (isNewBest) {
                Spacer(modifier = Modifier.height(12.dp))
                val infiniteTransition = rememberInfiniteTransition(label = "newbest")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                    label = "glow"
                )
                Text(
                    text = "NEW BEST!",
                    color = GameColors.ReviveGold.copy(alpha = glowAlpha),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Revive button (if eligible)
            if (isReviveEligible) {
                ReviveButton(onClick = onRevive)
                Spacer(modifier = Modifier.height(16.dp))
            }

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

            // Share + Menu row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.TileNormal,
                        contentColor = GameColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 104.dp, height = 48.dp)
                ) {
                    Text("SHARE", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onMenu,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.TileNormal,
                        contentColor = GameColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 104.dp, height = 48.dp)
                ) {
                    Text("MENU", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ReviveButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "revive")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "reviveGlow"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ReviveGold.copy(alpha = glowAlpha),
            contentColor = GameColors.Background
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(width = 260.dp, height = 52.dp)
    ) {
        Text("+5 SECONDS  (Watch Ad)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConfettiAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "confettiPhase"
    )

    val particles = remember {
        (0..30).map {
            Triple(
                Random.nextFloat(), // x
                Random.nextFloat(), // speed
                Random.nextInt(0, 5) // color index
            )
        }
    }

    val colors = listOf(
        GameColors.TileTarget,
        GameColors.Success,
        GameColors.ComboGlow,
        Color(0xFF93C5FD),
        Color(0xFFF472B6)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (baseX, speed, colorIdx) ->
            val x = baseX * size.width
            val y = ((phase * speed * 3) % 1.2f) * size.height
            val rotation = phase * 360 * speed
            val size_ = 6f + speed * 4f

            drawRect(
                color = colors[colorIdx],
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(size_, size_),
                alpha = 0.7f
            )
        }
    }
}
