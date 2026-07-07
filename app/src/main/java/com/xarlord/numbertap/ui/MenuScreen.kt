package com.xarlord.numbertap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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

@Composable
fun MenuScreen(
    highScore: Int,
    currentTheme: GameTheme,
    isHardMode: Boolean = false,
    coins: Int = 0,
    streak: Int = 0,
    onStartClick: () -> Unit,
    onTutorialClick: () -> Unit = {},
    onThemeChange: (GameTheme) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHardModeToggle: (Boolean) -> Unit = {},
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
        animationSpec = infiniteRepeatable(
            androidx.compose.animation.core.tween(1200, easing = LinearEasing),
            RepeatMode.Reverse
        ), label = "pulse"
    )

    // Logo float animation
    val logoFloat by infiniteTransition.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(
            androidx.compose.animation.core.tween(3000, easing = LinearEasing),
            RepeatMode.Reverse
        ), label = "logoFloat"
    )

    // Button pulse for first-time users
    var buttonScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(highScore) {
        if (highScore == 0) {
            while (true) {
                animate(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)
                ) { value, _ -> buttonScale = value }
                animate(
                    initialValue = 1.05f,
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)
                ) { value, _ -> buttonScale = value }
                kotlinx.coroutines.delay(800)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            colors.panelBackground,
                            colors.background
                        )
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating logo
            Box(modifier = Modifier.offset(y = logoFloat.dp)) {
                ThemedLogo(currentTheme, colors, style, 72.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.number_tap),
                color = colors.textPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.headerFontFamily,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                stringResource(R.string.the_ordered_grid),
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontFamily = style.bodyFontFamily,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (highScore > 0) {
                // High score badge with subtle background
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.tileTarget.copy(alpha = 0.1f))
                        .border(1.dp, colors.tileTarget.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.best_display, highScore),
                        color = colors.tileTarget,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = style.tileFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Coin and streak display
            if (coins > 0 || streak > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coins > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.panelBackground.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                stringResource(R.string.coins_display, coins),
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = style.tileFontFamily
                            )
                        }
                    }
                    if (streak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.panelBackground.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                stringResource(R.string.streak_display, streak),
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = style.tileFontFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // START button with elevation
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tileTarget,
                    contentColor = colors.textTarget
                ),
                shape = RoundedCornerShape(style.tileCornerRadius.dp),
                modifier = Modifier
                    .size(width = 220.dp, height = 58.dp)
                    .shadow(8.dp, RoundedCornerShape(style.tileCornerRadius.dp))
                    .semantics { contentDescription = startDesc }
            ) {
                Text(
                    stringResource(R.string.start),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.headerFontFamily,
                    letterSpacing = 3.sp
                )
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
                    modifier = Modifier.size(width = 220.dp, height = 46.dp)
                ) {
                    Text(stringResource(R.string.how_to_play), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = style.bodyFontFamily)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Normal / Hard mode toggle (#188)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.mode_normal),
                    color = if (!isHardMode) colors.tileTarget else colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (!isHardMode) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = style.bodyFontFamily,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!isHardMode) colors.tileTarget.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onHardModeToggle(false) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
                Text(
                    stringResource(R.string.mode_hard),
                    color = if (isHardMode) colors.failure else colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isHardMode) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = style.bodyFontFamily,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isHardMode) colors.failure.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onHardModeToggle(true) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
            if (isHardMode) {
                Text(
                    stringResource(R.string.mode_hard_desc),
                    color = colors.textSecondary.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = style.bodyFontFamily
                )
            }

            // Theme selector
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.style_label),
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontFamily = style.bodyFontFamily,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                            .padding(horizontal = 10.dp),
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
            Spacer(modifier = Modifier.height(20.dp))
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
