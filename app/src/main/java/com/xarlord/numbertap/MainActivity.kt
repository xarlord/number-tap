package com.xarlord.numbertap

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

@Composable
fun NumberTapApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var gameState by remember { mutableStateOf(GameState()) }
    val engine = remember { GameEngine() }
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    // Game tick loop
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.Game) {
            while (isActive && gameState.isPlaying) {
                delay(16) // ~60fps
                gameState = engine.tick(0.016)
                if (gameState.isGameOver) {
                    ActionLogger.logGameOver(gameState.score, gameState.highScore, gameState.timeRemaining)
                    currentScreen = Screen.GameOver
                }
            }
        }
    }

    when (currentScreen) {
        is Screen.Menu -> {
            MenuScreen(
                highScore = gameState.highScore,
                onStartClick = {
                    gameState = engine.startNewGame(gameState.highScore)
                    ActionLogger.logGameStart(0, gameState.highScore)
                    currentScreen = Screen.Game
                }
            )
        }
        is Screen.Game -> {
            GameScreen(
                gameState = gameState,
                onTileTap = { row, col ->
                    val tile = gameState.tiles.getOrNull(row)?.getOrNull(col)
                    val result = engine.onTap(row, col)
                    gameState = engine.getState()
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
                    // Clear shake offset after brief delay
                    if (result is TapResult.Wrong) {
                        engine.clearShake()
                        gameState = engine.getState()
                    }
                }
            )
        }
        is Screen.GameOver -> {
            GameOverScreen(
                score = gameState.score,
                highScore = gameState.highScore,
                onPlayAgain = {
                    gameState = engine.startNewGame(gameState.highScore)
                    ActionLogger.logGameStart(0, gameState.highScore)
                    currentScreen = Screen.Game
                },
                onMenu = {
                    currentScreen = Screen.Menu
                }
            )
        }
    }
}
