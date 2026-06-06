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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeConfig
import kotlin.random.Random

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    isReviveEligible: Boolean,
    currentTheme: GameTheme,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    onShare: () -> Unit = {},
    onRevive: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = ThemeConfig.colorsFor(currentTheme)
    val style = ThemeConfig.styleFor(currentTheme)
    val shape = RoundedCornerShape(style.tileCornerRadius.dp)

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
        if (isNewBest) ConfettiAnimation(colors)

        Column(
            modifier = Modifier.fillMaxSize().background(colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GAME OVER", color = colors.failure, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily)

            Spacer(modifier = Modifier.height(28.dp))
            Text("$displayScore", color = colors.textPrimary, fontSize = 52.sp, fontWeight = FontWeight.Bold, fontFamily = style.tileFontFamily)
            Spacer(modifier = Modifier.height(6.dp))
            Text("BEST: $highScore", color = colors.tileTarget, fontSize = 20.sp, fontFamily = style.bodyFontFamily)

            if (isNewBest) {
                Spacer(modifier = Modifier.height(10.dp))
                val inf = rememberInfiniteTransition(label = "nb")
                val glow by inf.animateFloat(0.6f, 1.0f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "g")
                Text("NEW BEST!", color = colors.tileTarget.copy(alpha = glow), fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily)
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isReviveEligible) {
                val inf = rememberInfiniteTransition(label = "rv")
                val rvGlow by inf.animateFloat(0.7f, 1.0f, infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "rv")
                Button(
                    onClick = onRevive,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.tileTarget.copy(alpha = rvGlow), contentColor = colors.textTarget),
                    shape = shape,
                    modifier = Modifier.size(width = 260.dp, height = 48.dp)
                ) { Text("+5 SECONDS  (Watch Ad)", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = style.bodyFontFamily) }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(onClick = onPlayAgain, colors = ButtonDefaults.buttonColors(containerColor = colors.tileTarget, contentColor = colors.textTarget), shape = shape, modifier = Modifier.size(width = 220.dp, height = 52.dp)) {
                Text("PLAY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onShare, colors = ButtonDefaults.buttonColors(containerColor = colors.tileBackground, contentColor = colors.textPrimary), shape = shape, modifier = Modifier.size(width = 104.dp, height = 44.dp)) {
                    Text("SHARE", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = style.bodyFontFamily)
                }
                Button(onClick = onMenu, colors = ButtonDefaults.buttonColors(containerColor = colors.tileBackground, contentColor = colors.textPrimary), shape = shape, modifier = Modifier.size(width = 104.dp, height = 44.dp)) {
                    Text("MENU", fontSize = 13.sp, fontFamily = style.bodyFontFamily)
                }
            }
        }
    }
}

@Composable
private fun ConfettiAnimation(colors: ThemeColors) {
    val inf = rememberInfiniteTransition(label = "cf")
    val phase by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "cf")
    val particles = remember { (0..25).map { Triple(Random.nextFloat(), Random.nextFloat(), Random.nextInt(0, 4)) } }
    val particleColors = listOf(colors.tileTarget, colors.success, colors.comboGlow, colors.failure)

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (bx, speed, ci) ->
            val x = bx * size.width
            val y = ((phase * speed * 3) % 1.2f) * size.height
            drawRect(particleColors[ci], topLeft = Offset(x, y), size = Size(5f + speed * 4f, 5f + speed * 4f), alpha = 0.7f)
        }
    }
}
