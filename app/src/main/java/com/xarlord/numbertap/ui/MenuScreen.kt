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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeConfig
import com.xarlord.numbertap.data.ThemeStyle
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MenuScreen(
    highScore: Int,
    currentTheme: GameTheme,
    onStartClick: () -> Unit,
    onTutorialClick: () -> Unit = {},
    onThemeChange: (GameTheme) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = ThemeConfig.colorsFor(currentTheme)
    val style = ThemeConfig.styleFor(currentTheme)

    // Hoist accessibility strings
    val startDesc = stringResource(R.string.a11y_start_game)
    val settingsDesc = stringResource(R.string.a11y_settings)

    val infiniteTransition = rememberInfiniteTransition(label = "menu")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Themed background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo — themed grid icon
            ThemedLogo(currentTheme, colors, style, 72.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                stringResource(R.string.number_tap),
                color = colors.textPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.headerFontFamily,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                stringResource(R.string.the_ordered_grid),
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontFamily = style.bodyFontFamily,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (highScore > 0) {
                Text(
                    stringResource(R.string.best_display, highScore),
                    color = colors.tileTarget,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.tileFontFamily
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // START button
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tileTarget,
                    contentColor = colors.textTarget
                ),
                shape = RoundedCornerShape(style.tileCornerRadius.dp),
                modifier = Modifier
                    .size(width = 200.dp, height = 56.dp)
                    .semantics { contentDescription = startDesc }
            ) {
                Text(stringResource(R.string.start), fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = style.headerFontFamily, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (highScore == 0) {
                Button(
                    onClick = onTutorialClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.tileBackground,
                        contentColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(style.tileCornerRadius.dp),
                    modifier = Modifier.size(width = 200.dp, height = 44.dp)
                ) {
                    Text(stringResource(R.string.how_to_play), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = style.bodyFontFamily)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Theme selector
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.style_label), color = colors.textSecondary, fontSize = 10.sp, fontFamily = style.bodyFontFamily, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameTheme.entries.forEach { theme ->
                    val tc = ThemeConfig.colorsFor(theme)
                    val isSelected = theme == currentTheme
                    val themeSelectDesc = stringResource(R.string.a11y_theme_select, theme.displayName)
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tc.background)
                            .then(
                                if (isSelected) Modifier.border(2.dp, tc.tileTarget, RoundedCornerShape(8.dp))
                                else Modifier.border(1.dp, tc.panelBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            )
                            .semantics {
                                contentDescription = themeSelectDesc
                                role = Role.Button
                            }
                            .clickable { onThemeChange(theme) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            theme.displayName,
                            color = tc.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Settings button
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_button),
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontFamily = style.bodyFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .semantics {
                        contentDescription = settingsDesc
                        role = Role.Button
                    }
                    .clickable { onSettingsClick() }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun ThemedLogo(
    theme: GameTheme,
    colors: ThemeColors,
    style: ThemeStyle,
    size: androidx.compose.ui.unit.Dp
) {
    val cornerRadius = style.tileCornerRadius

    // Hoist Paint to avoid allocation on every draw frame (fixes #116)
    val paint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = Modifier.size(size)) {
        val tileSize = this.size.width / 3
        for (row in 0..2) {
            for (col in 0..2) {
                val idx = row * 3 + col
                val bgColor = when {
                    idx == 4 -> colors.tileTarget
                    else -> colors.tileBackground
                }
                val x = col * tileSize + 2f
                val y = row * tileSize + 2f
                val s = tileSize - 4f

                if (cornerRadius > 0) {
                    drawRoundRect(
                        color = bgColor,
                        topLeft = Offset(x, y),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(cornerRadius * density, cornerRadius * density)
                    )
                } else {
                    drawRect(bgColor, topLeft = Offset(x, y), size = Size(s, s))
                    // Terminal/Matrix: draw border
                    if (style.showTileBorder) {
                        drawRect(
                            colors.panelBorder,
                            topLeft = Offset(x, y),
                            size = Size(s, s),
                            style = Stroke(width = 1.5f * density)
                        )
                    }
                }

                if (idx < 3) {
                    val c = if (idx == 4) colors.textTarget else colors.textPrimary
                    val argb = (c.alpha * 255).toInt() shl 24 or
                               (c.red * 255).toInt() shl 16 or
                               (c.green * 255).toInt() shl 8 or
                               (c.blue * 255).toInt()
                    paint.color = argb
                    paint.textSize = s * 0.4f
                    paint.typeface = if (style.tileFontFamily == FontFamily.Monospace) android.graphics.Typeface.MONOSPACE else android.graphics.Typeface.DEFAULT_BOLD
                    drawContext.canvas.nativeCanvas.drawText(
                        "${idx + 1}",
                        x + s / 2,
                        y + s * 0.65f,
                        paint
                    )
                }
            }
        }
    }
}
