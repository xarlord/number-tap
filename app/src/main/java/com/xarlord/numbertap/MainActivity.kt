package com.xarlord.numbertap

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.xarlord.numbertap.data.GameState
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
        setContent {
            NumberTapApp()
        }
    }
}

sealed class Screen {
    object Menu : Screen()
    object Game : Screen()
    object GameOver : Screen()
}

// High score persistence keys
private const val PREFS_NAME = "number_tap_prefs"
private const val KEY_HIGH_SCORE = "high_score"

private fun loadHighScore(context: Context): Int {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_HIGH_SCORE, 0)
}

private fun saveHighScore(context: Context, score: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_HIGH_SCORE, score)
        .apply()
}

@Composable
fun NumberTapApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var gameState by remember { mutableStateOf(GameState()) }
    val engine = remember { GameEngine() }
    val context = LocalContext.current
    var highScore by remember { mutableStateOf(loadHighScore(context)) }

    // #29: SoundManager lifecycle — release on dispose
    val soundManager = remember {
        SoundManager(context)
    }
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    // Game tick loop
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.Game) {
            while (isActive && gameState.isPlaying) {
                delay(16) // ~60fps
                gameState = engine.tick(gameState, 0.016)
                if (gameState.isGameOver) {
                    // Persist high score
                    if (gameState.highScore > highScore) {
                        highScore = gameState.highScore
                        saveHighScore(context, gameState.highScore)
                    }
                    ActionLogger.logGameOver(gameState.score, gameState.highScore, gameState.timeRemaining)
                    currentScreen = Screen.GameOver
                }
            }
        }
    }

    when (currentScreen) {
        is Screen.Menu -> {
            MenuScreen(
                highScore = highScore,
                onStartClick = {
                    gameState = engine.startNewGame(highScore)
                    ActionLogger.logGameStart(0, highScore)
                    currentScreen = Screen.Game
                }
            )
        }
        is Screen.Game -> {
            GameScreen(
                gameState = gameState,
                onTileTap = { row, col ->
                    val tile = gameState.tiles.getOrNull(row)?.getOrNull(col)
                    val (newState, result) = engine.onTap(gameState, row, col)
                    gameState = newState
                    when (result) {
                        is TapResult.Correct -> {
                            ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber - 1, true, gameState.score, gameState.timeRemaining)
                            soundManager.playSuccess(result.combo)
                        }
                        is TapResult.Wrong -> {
                            ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber, false, gameState.score, gameState.timeRemaining)
                            soundManager.playFailure()
                        }
                        is TapResult.Invalid -> {}
                    }
                },
                onFeedbackComplete = {
                    // #31: Clear feedback states after 3-frame flash
                    gameState = engine.resetTileStates(gameState)
                    gameState = engine.clearShake(gameState)
                }
            )
        }
        is Screen.GameOver -> {
            GameOverScreen(
                score = gameState.score,
                highScore = gameState.highScore,
                onPlayAgain = {
                    gameState = engine.startNewGame(highScore)
                    ActionLogger.logGameStart(0, highScore)
                    currentScreen = Screen.Game
                },
                onMenu = {
                    currentScreen = Screen.Menu
                }
            )
        }
    }
}
