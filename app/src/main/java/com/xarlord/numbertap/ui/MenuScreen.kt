package com.xarlord.numbertap.ui

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MenuScreen(
    highScore: Int,
    onStartClick: () -> Unit,
    onTutorialClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Animated floating numbers background
        FloatingNumbersBackground()

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GameColors.Background.copy(alpha = 0.85f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom logo — stylized grid icon
            Canvas(modifier = Modifier.size(80.dp)) {
                val tileSize = size.width / 3
                for (row in 0..2) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val tileColor = when {
                            idx == 4 -> GameColors.TileTarget // Center tile highlighted
                            else -> GameColors.TileNormal
                        }
                        drawRoundRect(
                            color = tileColor,
                            topLeft = Offset(col * tileSize + 3f, row * tileSize + 3f),
                            size = androidx.compose.ui.geometry.Size(tileSize - 6f, tileSize - 6f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        if (idx < 3) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "${idx + 1}",
                                col * tileSize + tileSize / 2,
                                row * tileSize + tileSize * 0.65f,
                                android.graphics.Paint().apply {
                                    setColor(android.graphics.Color.WHITE)
                                    textSize = tileSize * 0.4f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    isAntiAlias = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "NUMBER TAP",
                color = GameColors.TextPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "The Ordered Grid",
                color = GameColors.TextSecondary,
                fontSize = 16.sp,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (highScore > 0) {
                // High score with glow effect
                Box(
                    modifier = Modifier
                        .drawBehind {
                            drawCircle(
                                color = GameColors.TileTarget.copy(alpha = glowAlpha),
                                radius = size.width * 0.6f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BEST: $highScore",
                        color = GameColors.TileTarget,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // START button with pulse
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameColors.TileTarget,
                    contentColor = GameColors.Background
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp)
                    .offset(y = (pulseScale * 2 - 2).dp)
            ) {
                Text("START", fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tutorial button
            if (highScore == 0) {
                Button(
                    onClick = onTutorialClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.TileNormal,
                        contentColor = GameColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 200.dp, height = 48.dp)
                ) {
                    Text("HOW TO PLAY", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun FloatingNumbersBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "phase"
    )

    // Generate stable random positions
    val particles = remember {
        (0..20).map { i ->
            Random.nextFloat() to Random.nextFloat() // (x, y) in 0..1
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEachIndexed { idx, (baseX, baseY) ->
            val number = (idx % 9) + 1
            val x = baseX * width
            val y = (baseY * height + phase * 0.5f * (idx % 3 + 1)) % height
            val alpha = 0.06f + 0.04f * sin(Math.toRadians((phase + idx * 30.0).toDouble())).toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                "$number",
                x,
                y,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
                    textSize = 28f + idx % 3 * 8f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}

private fun DrawScope.drawRoundRect(
    color: Color,
    topLeft: Offset,
    size: androidx.compose.ui.geometry.Size,
    cornerRadius: androidx.compose.ui.geometry.CornerRadius
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius
    )
}
