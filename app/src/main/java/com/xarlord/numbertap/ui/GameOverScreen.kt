package com.xarlord.numbertap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeConfig
import kotlin.random.Random

// Animation & layout constants
private const val SCORE_REVEAL_DELAY_MS = 300L
private const val SCORE_REVEAL_DURATION_MS = 800
private const val TITLE_INITIAL_SCALE = 0.5f
private const val CONFETTI_PARTICLE_COUNT = 30
private const val CONFETTI_DURATION_MS = 3000
private const val CONFETTI_ALPHA = 0.7f

// Confetti particle generation seeds
private const val CONFETTI_SEED_X = 42
private const val CONFETTI_SEED_SPEED = 137
private const val CONFETTI_SEED_COLOR = 256
private const val CONFETTI_COLOR_COUNT = 4

// Confetti physics
private const val CONFETTI_SPEED_MULTIPLIER = 3f
private const val CONFETTI_WRAP_FACTOR = 1.2f
private const val CONFETTI_BASE_SIZE = 4f
private const val CONFETTI_SIZE_VARIANCE = 5f

// Background gradient
private const val BG_GRADIENT_MID_ALPHA = 0.8f

// Score card
private const val SCORE_CARD_CORNER_DP = 16
private const val SCORE_CARD_BG_ALPHA = 0.6f
private const val SCORE_CARD_BORDER_ALPHA = 0.4f
private const val SCORE_CARD_PADDING_H_DP = 48
private const val SCORE_CARD_PADDING_V_DP = 24

// Score text
private const val SCORE_FONT_SIZE_SP = 56
private const val BEST_LABEL_FONT_SIZE_SP = 18
private const val GAME_OVER_FONT_SIZE_SP = 32
private const val SCORE_BEST_SPACER_DP = 8
private const val BORDER_WIDTH_DP = 1

// New-best badge
private const val NEW_BEST_GLOW_MIN = 0.6f
private const val NEW_BEST_GLOW_DURATION_MS = 500
private const val NEW_BEST_CORNER_DP = 20
private const val NEW_BEST_BG_ALPHA = 0.15f
private const val NEW_BEST_PADDING_H_DP = 24
private const val NEW_BEST_PADDING_V_DP = 8
private const val NEW_BEST_FONT_SIZE_SP = 18

// Revive button
private const val REVIVE_BTN_WIDTH_DP = 260
private const val REVIVE_BTN_HEIGHT_DP = 50
private const val REVIVE_BTN_SHADOW_DP = 4
private const val REVIVE_FONT_SIZE_SP = 14
private const val REVIVE_GLOW_MIN = 0.7f
private const val REVIVE_GLOW_DURATION_MS = 400

// Layout spacing
private const val SECTION_SPACING_DP = 32
private const val INTER_BUTTON_SPACING_DP = 12

// Play Again button
private const val PLAY_AGAIN_BTN_WIDTH_DP = 220
private const val PLAY_AGAIN_BTN_HEIGHT_DP = 54
private const val PLAY_AGAIN_BTN_SHADOW_DP = 6
private const val PLAY_AGAIN_FONT_SIZE_SP = 20

// Share / Menu buttons
private const val SECONDARY_BTN_WIDTH_DP = 104
private const val SECONDARY_BTN_HEIGHT_DP = 46
private const val SECONDARY_FONT_SIZE_SP = 13

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    isReviveEligible: Boolean,
    currentTheme: GameTheme,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
    coinBalance: Int = 0,
    onShare: () -> Unit = {},
    onRevive: () -> Unit = {},
    onSpendCoins: () -> Unit = {}
) {
    val colors = ThemeConfig.colorsFor(currentTheme)
    val style = ThemeConfig.styleFor(currentTheme)
    val shape = RoundedCornerShape(style.tileCornerRadius.dp)

    // Hoist accessibility strings
    val finalScoreDesc = stringResource(R.string.a11y_final_score, score)
    val bestScoreDesc = stringResource(R.string.a11y_best_score, highScore)
    val newBestDesc = stringResource(R.string.a11y_new_best)
    val reviveDesc = stringResource(R.string.a11y_revive)
    val playAgainDesc = stringResource(R.string.a11y_play_again)
    val shareDesc = stringResource(R.string.a11y_share_score)
    val menuDesc = stringResource(R.string.a11y_go_menu)

    // Phase 3.5: Smooth animated score counter with spring
    var displayScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(score) {
        // Dramatic pause before reveal
        kotlinx.coroutines.delay(SCORE_REVEAL_DELAY_MS)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(SCORE_REVEAL_DURATION_MS, easing = LinearEasing)
        ) { value, _ ->
            displayScore = (score * value).toInt()
        }
        displayScore = score
    }

    // Scale animation for "GAME OVER" text
    var titleScale by remember { mutableFloatStateOf(TITLE_INITIAL_SCALE) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = TITLE_INITIAL_SCALE,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) { value, _ -> titleScale = value }
    }

    val isNewBest = isNewHighScore && score > 0

    Box(modifier = modifier.fillMaxSize()) {
        if (isNewBest) ConfettiAnimation(colors)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            colors.panelBackground.copy(alpha = BG_GRADIENT_MID_ALPHA),
                            colors.background
                        )
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "GAME OVER" with spring scale-in
            Text(
                stringResource(R.string.game_over),
                color = colors.failure,
                fontSize = GAME_OVER_FONT_SIZE_SP.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.headerFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = titleScale
                    scaleY = titleScale
                }
            )

            Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))

            // Score in a card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(SCORE_CARD_CORNER_DP.dp))
                    .background(colors.panelBackground.copy(alpha = SCORE_CARD_BG_ALPHA))
                    .border(BORDER_WIDTH_DP.dp, colors.panelBorder.copy(alpha = SCORE_CARD_BORDER_ALPHA), RoundedCornerShape(SCORE_CARD_CORNER_DP.dp))
                    .padding(horizontal = SCORE_CARD_PADDING_H_DP.dp, vertical = SCORE_CARD_PADDING_V_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$displayScore",
                        color = colors.textPrimary,
                        fontSize = SCORE_FONT_SIZE_SP.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = style.tileFontFamily,
                        modifier = Modifier.semantics { contentDescription = finalScoreDesc }
                    )
                    Spacer(modifier = Modifier.height(SCORE_BEST_SPACER_DP.dp))
                    Text(
                        stringResource(R.string.best_label, highScore),
                        color = colors.tileTarget,
                        fontSize = BEST_LABEL_FONT_SIZE_SP.sp,
                        fontFamily = style.bodyFontFamily,
                        modifier = Modifier.semantics { contentDescription = bestScoreDesc }
                    )
                }
            }

            if (isNewBest) {
                Spacer(modifier = Modifier.height(INTER_BUTTON_SPACING_DP.dp))
                val inf = rememberInfiniteTransition(label = "nb")
                val glow by inf.animateFloat(NEW_BEST_GLOW_MIN, 1.0f, infiniteRepeatable(tween(NEW_BEST_GLOW_DURATION_MS), RepeatMode.Reverse), label = "g")
                // New best badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NEW_BEST_CORNER_DP.dp))
                        .background(colors.tileTarget.copy(alpha = NEW_BEST_BG_ALPHA))
                        .border(BORDER_WIDTH_DP.dp, colors.tileTarget.copy(alpha = glow), RoundedCornerShape(NEW_BEST_CORNER_DP.dp))
                        .padding(horizontal = NEW_BEST_PADDING_H_DP.dp, vertical = NEW_BEST_PADDING_V_DP.dp)
                ) {
                    Text(
                        stringResource(R.string.new_best),
                        color = colors.tileTarget.copy(alpha = glow),
                        fontSize = NEW_BEST_FONT_SIZE_SP.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = style.headerFontFamily,
                        modifier = Modifier.semantics { contentDescription = newBestDesc }
                    )
                }
            }

            Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))

            if (isReviveEligible) {
                val inf = rememberInfiniteTransition(label = "rv")
                val rvGlow by inf.animateFloat(REVIVE_GLOW_MIN, 1.0f, infiniteRepeatable(tween(REVIVE_GLOW_DURATION_MS), RepeatMode.Reverse), label = "rv")
                Button(
                    onClick = onRevive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.tileTarget.copy(alpha = rvGlow),
                        contentColor = colors.textTarget
                    ),
                    shape = shape,
                    modifier = Modifier
                        .size(width = REVIVE_BTN_WIDTH_DP.dp, height = REVIVE_BTN_HEIGHT_DP.dp)
                        .shadow(REVIVE_BTN_SHADOW_DP.dp, shape)
                        .semantics {
                            contentDescription = reviveDesc
                            role = Role.Button
                        }
                ) {
                    Text(
                        stringResource(R.string.revive_button),
                        fontSize = REVIVE_FONT_SIZE_SP.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = style.bodyFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(INTER_BUTTON_SPACING_DP.dp))
            }

            // Coin balance display + spend coins for revive
            val canAffordRevive = coinBalance >= com.xarlord.numbertap.data.GameConfig.COIN_COST_FOR_REVIVE
            // #215: use localized string resources instead of hardcoded English
            val coinBalanceStr = stringResource(R.string.coins_display, coinBalance)
            // #226: use plural resource for correct i18n of "Coin(s)"
            val coinCost = com.xarlord.numbertap.data.GameConfig.COIN_COST_FOR_REVIVE
            val reviveBonus = com.xarlord.numbertap.data.GameConfig.REVIVE_BONUS_SECONDS.toInt()
            val spendCoinsStr = pluralStringResource(
                R.plurals.spend_coins_revive,
                coinCost,
                coinCost,
                reviveBonus
            )
            Text(
                coinBalanceStr,
                color = colors.textSecondary,
                fontSize = 16.sp,
                fontFamily = style.bodyFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSpendCoins,
                enabled = canAffordRevive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAffordRevive) colors.comboGlow else colors.panelBackground,
                    contentColor = if (canAffordRevive) colors.textTarget else colors.textSecondary,
                    disabledContainerColor = colors.panelBackground,
                    disabledContentColor = colors.textSecondary
                ),
                shape = shape,
                modifier = Modifier
                    .size(width = REVIVE_BTN_WIDTH_DP.dp, height = REVIVE_BTN_HEIGHT_DP.dp)
                    .shadow(REVIVE_BTN_SHADOW_DP.dp, shape)
            ) {
                Text(
                    spendCoinsStr,
                    fontSize = REVIVE_FONT_SIZE_SP.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.bodyFontFamily
                )
            }
            Spacer(modifier = Modifier.height(INTER_BUTTON_SPACING_DP.dp))

            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tileTarget,
                    contentColor = colors.textTarget
                ),
                shape = shape,
                modifier = Modifier
                    .size(width = PLAY_AGAIN_BTN_WIDTH_DP.dp, height = PLAY_AGAIN_BTN_HEIGHT_DP.dp)
                    .shadow(PLAY_AGAIN_BTN_SHADOW_DP.dp, shape)
                    .semantics { contentDescription = playAgainDesc; role = Role.Button }
            ) {
                Text(
                    stringResource(R.string.play_again),
                    fontSize = PLAY_AGAIN_FONT_SIZE_SP.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.headerFontFamily
                )
            }
            Spacer(modifier = Modifier.height(INTER_BUTTON_SPACING_DP.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(INTER_BUTTON_SPACING_DP.dp)) {
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.tileBackground,
                        contentColor = colors.textPrimary
                    ),
                    shape = shape,
                    modifier = Modifier
                        .size(width = SECONDARY_BTN_WIDTH_DP.dp, height = SECONDARY_BTN_HEIGHT_DP.dp)
                        .semantics { contentDescription = shareDesc; role = Role.Button }
                ) {
                    Text(stringResource(R.string.share), fontSize = SECONDARY_FONT_SIZE_SP.sp, fontWeight = FontWeight.Bold, fontFamily = style.bodyFontFamily)
                }
                Button(
                    onClick = onMenu,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.tileBackground,
                        contentColor = colors.textPrimary
                    ),
                    shape = shape,
                    modifier = Modifier
                        .size(width = SECONDARY_BTN_WIDTH_DP.dp, height = SECONDARY_BTN_HEIGHT_DP.dp)
                        .semantics { contentDescription = menuDesc; role = Role.Button }
                ) {
                    Text(stringResource(R.string.menu), fontSize = SECONDARY_FONT_SIZE_SP.sp, fontFamily = style.bodyFontFamily)
                }
            }
        }
    }
}

@Composable
private fun ConfettiAnimation(colors: ThemeColors) {
    val inf = rememberInfiniteTransition(label = "cf")
    val phase by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(CONFETTI_DURATION_MS, easing = LinearEasing)), label = "cf")
    val particles = remember {
        (0..CONFETTI_PARTICLE_COUNT).map {
            Triple(
                Random(seed = CONFETTI_SEED_X + it).nextFloat(),
                Random(seed = CONFETTI_SEED_SPEED + it).nextFloat(),
                Random(seed = CONFETTI_SEED_COLOR + it).nextInt(0, CONFETTI_COLOR_COUNT)
            )
        }
    }
    val particleColors = listOf(colors.tileTarget, colors.success, colors.comboGlow, colors.failure)

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (bx, speed, ci) ->
            val x = bx * size.width
            val y = ((phase * speed * CONFETTI_SPEED_MULTIPLIER) % CONFETTI_WRAP_FACTOR) * size.height
            val particleSize = CONFETTI_BASE_SIZE + speed * CONFETTI_SIZE_VARIANCE
            // Mix rectangles and circles for variety
            if (ci % 2 == 0) {
                drawRect(particleColors[ci], topLeft = Offset(x, y), size = Size(particleSize, particleSize), alpha = CONFETTI_ALPHA)
            } else {
                drawCircle(particleColors[ci], radius = particleSize / 2, center = Offset(x + particleSize / 2, y + particleSize / 2), alpha = CONFETTI_ALPHA)
            }
        }
    }
}
