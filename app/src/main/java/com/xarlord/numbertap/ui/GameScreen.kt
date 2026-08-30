package com.xarlord.numbertap.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameConfig
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.TierAnnouncement
import com.xarlord.numbertap.data.ThemeConfig

@Composable
fun GameScreen(
    gameState: GameState,
    onTileTap: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    onPauseClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val theme = gameState.currentTheme
    val colors = ThemeConfig.colorsFor(theme)
    val style = ThemeConfig.styleFor(theme)

    val infiniteTransition = rememberInfiniteTransition(label = "game")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "pulse"
    )
    val urgentPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "urgent"
    )

    val strRound2 = stringResource(R.string.round_2)
    val strHardMode = stringResource(R.string.hard_mode)
    val strTutorialTapOrder = stringResource(R.string.tutorial_tap_order)
    val strTutorialKeepGoing = stringResource(R.string.tutorial_keep_going)
    val strTutorialAlmost = stringResource(R.string.tutorial_almost)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // === TOP: Stats Bar (fixed height) ===
            TopBar(gameState, colors, style, onPauseClick, urgentPulse)

            // Timer bar
            TimerBar(gameState.timeRemaining, GameConfig.INITIAL_TIME_SECONDS, colors)

            if (gameState.isTutorial) {
                Text(
                    when {
                        gameState.score < 2 -> strTutorialTapOrder
                        gameState.score < 4 -> strTutorialKeepGoing
                        else -> strTutorialAlmost
                    },
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    fontFamily = style.bodyFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // === CENTER: Game Grid (takes remaining space) ===
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Target hint
                    TargetHint(
                        targetNumber = gameState.targetNumber,
                        colors = colors,
                        style = style,
                        pulseAlpha = if (gameState.isTutorial) pulseAlpha else 1f,
                        isTutorial = gameState.isTutorial
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid
                    GridContainer(
                        tiles = gameState.tiles,
                        targetNumber = gameState.targetNumber,
                        shakeOffsetPx = gameState.shakeOffset,
                        onTileTap = onTileTap,
                        isTutorial = gameState.isTutorial,
                        isHardMode = gameState.isHardMode,
                        hiddenTileIds = gameState.hiddenTileIds,
                        animatingTileId = gameState.animatingTileId,
                        theme = theme,
                        colors = colors,
                        style = style
                    )
                }

            }

            // === BOTTOM: Stats Panel (fixed height) ===
            BottomPanel(gameState, colors, style)
        }

        // Scanline overlay for Terminal theme
        if (style.showScanlines) {
            ScanlineOverlay()
        }

        // Pause overlay
        if (gameState.isPaused) {
            PauseOverlay(colors, style, onResume = onPauseClick, onMenu = onMenuClick)
        }

        // Urgency vignette
        if (gameState.timeRemaining < 5 && gameState.isPlaying && !gameState.isPaused) {
            UrgencyVignette(colors.vignetteColor, urgentPulse)
        }

        // Tier announcement — overlaid on top of grid, does NOT push content
        gameState.tierAnnouncement?.let { ann ->
            val announcementText = when (ann) {
                TierAnnouncement.ROUND_2 -> strRound2
                TierAnnouncement.HARD_MODE -> strHardMode
                TierAnnouncement.INSANE_MODE -> stringResource(R.string.insane_mode)
                TierAnnouncement.NICE -> stringResource(R.string.nice)
                TierAnnouncement.GREAT -> stringResource(R.string.great)
                TierAnnouncement.AMAZING -> stringResource(R.string.amazing)
                TierAnnouncement.LEGENDARY -> stringResource(R.string.legendary)
            }
            Text(
                announcementText,
                color = when (ann) {
                    TierAnnouncement.ROUND_2 -> colors.timerWarning
                    TierAnnouncement.HARD_MODE -> colors.timerUrgent
                    TierAnnouncement.INSANE_MODE -> colors.failure
                    else -> colors.tileTarget
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.headerFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .offset(y = (-80).dp)
            )
        }

        // Combo text — overlaid on top of grid, below timer, does NOT push content
        if (gameState.comboCount > 1) {
            Text(
                stringResource(R.string.combo_format, gameState.comboCount),
                color = colors.comboGlow.copy(alpha = pulseAlpha),
                fontSize = (16 + gameState.comboCount * 2).coerceAtMost(28).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.headerFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .offset(y = (-130).dp)
            )
        }


    }
}
