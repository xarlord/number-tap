package com.xarlord.numbertap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.TileState
import com.xarlord.numbertap.game.ActionLogger
import com.xarlord.numbertap.game.GameEngine
import com.xarlord.numbertap.game.TapResult
import com.xarlord.numbertap.audio.SoundManager
import com.xarlord.numbertap.ui.GameOverScreen
import com.xarlord.numbertap.ui.GameScreen
import com.xarlord.numbertap.ui.MenuScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NumberTapApp() }
    }
}

sealed class Screen {
    data object Menu : Screen()
    data object Game : Screen()
    data object GameOver : Screen()
}

private const val PREFS_NAME = "number_tap_prefs"
private const val KEY_HIGH_SCORE = "high_score"
private const val KEY_HAS_PLAYED = "has_played"

private fun loadHighScore(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_HIGH_SCORE, 0)

private fun saveHighScore(context: Context, score: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_HIGH_SCORE, score).apply()
}

private fun hasPlayedBefore(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_HAS_PLAYED, false)

private fun markPlayed(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_HAS_PLAYED, true).apply()
}

@Composable
fun NumberTapApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var gameState by remember { mutableStateOf(GameState()) }
    val engine = remember { GameEngine() }
    val context = LocalContext.current
    var highScore by remember { mutableStateOf(loadHighScore(context)) }
    var isTutorialMode by remember { mutableStateOf(false) }
    var lastTickTime by remember { mutableLongStateOf(0L) }
    var lastCountdownTickSecond by remember { mutableIntStateOf(-1) }

    val soundManager = remember { SoundManager(context) }
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Single LaunchedEffect for tick + feedback
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.Game) {
            lastTickTime = System.currentTimeMillis()
            while (isActive) {
                delay(16)

                val now = System.currentTimeMillis()

                // Feedback duration: 60ms shake + flash
                val hasFeedback = gameState.tiles.any { row -> row.any { it.state != TileState.ACTIVE } }
                if (hasFeedback) {
                    delay(60)
                    gameState = engine.resetTileStates(gameState)
                    gameState = engine.clearShake(gameState)
                }

                // Clear expired floating texts
                gameState = engine.clearExpiredFloatingTexts(gameState, now)

                // Clear tier announcement after 1.5s
                if (gameState.tierAnnouncement != null) {
                    delay(1500)
                    gameState = engine.clearTierAnnouncement(gameState)
                }

                // Game tick
                if (gameState.isPlaying && !gameState.isPaused) {
                    val delta = (now - lastTickTime) / 1000.0
                    lastTickTime = now
                    gameState = engine.tick(gameState, delta)

                    // Countdown tick sound at <5 seconds
                    val currentSecond = gameState.timeRemaining.toInt()
                    if (gameState.timeRemaining < 5.0 && gameState.timeRemaining > 0.0 && currentSecond != lastCountdownTickSecond) {
                        lastCountdownTickSecond = currentSecond
                        soundManager.playCountdownTick()
                    }

                    // Game over
                    if (gameState.isGameOver) {
                        if (gameState.highScore > highScore) {
                            highScore = gameState.highScore
                            saveHighScore(context, gameState.highScore)
                        }
                        markPlayed(context)
                        ActionLogger.logGameOver(gameState.score, gameState.highScore, gameState.timeRemaining)
                        soundManager.playGameOver()
                        soundManager.stopBGMusic()
                        currentScreen = Screen.GameOver
                    }
                } else {
                    lastTickTime = now
                }
            }
        }
    }

    when (currentScreen) {
        is Screen.Menu -> MenuScreen(
            highScore = highScore,
            onStartClick = {
                gameState = engine.startNewGame(highScore)
                isTutorialMode = false
                ActionLogger.logGameStart(0, highScore)
                soundManager.startBGMusic()
                currentScreen = Screen.Game
            },
            onTutorialClick = {
                gameState = engine.startTutorial(highScore)
                isTutorialMode = true
                ActionLogger.logTutorialStart()
                currentScreen = Screen.Game
            }
        )

        is Screen.Game -> GameScreen(
            gameState = gameState,
            onTileTap = { row, col ->
                val tile = gameState.tiles.getOrNull(row)?.getOrNull(col)
                val (newState, result) = engine.onTap(gameState, row, col)
                gameState = newState
                when (result) {
                    is TapResult.Correct -> {
                        ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber - 1, true, gameState.score, gameState.timeRemaining)
                        soundManager.playSuccess(result.combo)
                        // Milestone sounds
                        if (gameState.score % 10 == 0 && gameState.score > 0) {
                            soundManager.playMilestone()
                        }
                    }
                    is TapResult.Wrong -> {
                        ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber, false, gameState.score, gameState.timeRemaining)
                        soundManager.playFailure()
                        if (gameState.comboCount > 2) {
                            soundManager.playComboBreak()
                        }
                    }
                    is TapResult.Invalid -> {}
                }
            },
            onPauseClick = {
                if (gameState.isPaused) {
                    gameState = engine.resume(gameState)
                    lastTickTime = System.currentTimeMillis()
                } else {
                    gameState = engine.pause(gameState)
                }
            }
        )

        is Screen.GameOver -> GameOverScreen(
            score = gameState.score,
            highScore = gameState.highScore,
            isNewHighScore = gameState.isNewHighScore,
            isReviveEligible = engine.isReviveEligible(gameState),
            onPlayAgain = {
                gameState = engine.startNewGame(highScore)
                isTutorialMode = false
                ActionLogger.logGameStart(0, highScore)
                soundManager.startBGMusic()
                currentScreen = Screen.Game
            },
            onMenu = {
                soundManager.stopBGMusic()
                currentScreen = Screen.Menu
            },
            onShare = {
                ActionLogger.logShare(gameState.score)
                shareScore(context, gameState.score, gameState.highScore)
            },
            onRevive = {
                gameState = engine.revive(gameState)
                lastTickTime = System.currentTimeMillis()
                soundManager.startBGMusic()
                currentScreen = Screen.Game
            }
        )
    }
}

private fun shareScore(context: Context, score: Int, highScore: Int) {
    val text = """
        Number Tap - The Ordered Grid
        
        Score: $score
        Personal Best: $highScore
        
        Can you beat my score?
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Score"))
}
