package com.xarlord.numbertap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.TileState
import com.xarlord.numbertap.game.ActionLogger
import com.xarlord.numbertap.game.GameEngine
import com.xarlord.numbertap.game.TapResult
import com.xarlord.numbertap.audio.SoundManager
import com.xarlord.numbertap.ui.GameOverScreen
import com.xarlord.numbertap.ui.GameScreen
import com.xarlord.numbertap.ui.MenuScreen
import com.xarlord.numbertap.ui.SettingsScreen
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
    data object Settings : Screen()
}

private const val PREFS_NAME = "number_tap_prefs"
private const val KEY_HIGH_SCORE = "high_score"
private const val KEY_HAS_PLAYED = "has_played"
private const val KEY_THEME = "selected_theme"
private const val KEY_SOUND_ENABLED = "sound_enabled"
private const val KEY_MUSIC_ENABLED = "music_enabled"

private fun loadHighScore(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_HIGH_SCORE, 0)

private fun saveHighScore(context: Context, score: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_HIGH_SCORE, score).apply()
}

private fun loadTheme(context: Context): GameTheme {
    val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_THEME, GameTheme.DEFAULT.name)
    return try { GameTheme.valueOf(name ?: GameTheme.DEFAULT.name) } catch (_: Exception) { GameTheme.DEFAULT }
}

private fun saveTheme(context: Context, theme: GameTheme) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_THEME, theme.name).apply()
}

private fun hasPlayedBefore(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_HAS_PLAYED, false)

private fun markPlayed(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_HAS_PLAYED, true).apply()
}

private fun loadSoundEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SOUND_ENABLED, true)

private fun saveSoundEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
}

private fun loadMusicEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_MUSIC_ENABLED, true)

private fun saveMusicEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
}

@Composable
fun NumberTapApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var gameState by remember { mutableStateOf(GameState()) }
    val engine = remember { GameEngine() }
    val context = LocalContext.current
    var highScore by remember { mutableStateOf(loadHighScore(context)) }
    var selectedTheme by remember { mutableStateOf(loadTheme(context)) }
    var soundEnabled by remember { mutableStateOf(loadSoundEnabled(context)) }
    var musicEnabled by remember { mutableStateOf(loadMusicEnabled(context)) }
    var lastTickTime by remember { mutableLongStateOf(0L) }
    var lastCountdownTickSecond by remember { mutableIntStateOf(-1) }

    val soundManager = remember { SoundManager(context) }
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Game loop
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.Game) {
            lastTickTime = SystemClock.elapsedRealtime()
            while (isActive) {
                try {
                    delay(16)
                    val now = SystemClock.elapsedRealtime()

                    val hasFeedback = gameState.tiles.any { row -> row.any { it.state != TileState.ACTIVE } }
                    if (hasFeedback) {
                        delay(60)
                        gameState = engine.resetTileStates(gameState)
                        gameState = engine.clearShake(gameState)
                        // Reset delta after feedback freeze so timer doesn't tick during visual pause (fixes #118)
                        lastTickTime = SystemClock.elapsedRealtime()
                    }

                    gameState = engine.clearExpiredFloatingTexts(gameState, now)

                    if (gameState.tierAnnouncement != null) {
                        delay(1500)
                        gameState = engine.clearTierAnnouncement(gameState)
                        // Also reset delta after tier announcement pause
                        lastTickTime = SystemClock.elapsedRealtime()
                    }

                    if (gameState.isPlaying && !gameState.isPaused) {
                        val delta = (now - lastTickTime) / 1000.0
                        lastTickTime = now
                        gameState = engine.tick(gameState, delta)

                        val currentSecond = gameState.timeRemaining.toInt()
                        if (soundEnabled && gameState.timeRemaining < 5.0 && gameState.timeRemaining > 0.0 && currentSecond != lastCountdownTickSecond) {
                            lastCountdownTickSecond = currentSecond
                            soundManager.playCountdownTick()
                        }

                        if (gameState.isGameOver) {
                            if (gameState.highScore > highScore) {
                                highScore = gameState.highScore
                                saveHighScore(context, gameState.highScore)
                            }
                            markPlayed(context)
                            ActionLogger.logGameOver(gameState.score, gameState.highScore, gameState.timeRemaining)
                            if (soundEnabled) soundManager.playGameOver()
                            soundManager.stopBGMusic()
                            currentScreen = Screen.GameOver
                        }
                    } else {
                        lastTickTime = now
                    }
                } catch (e: Exception) {
                    ActionLogger.logError("game_loop", e.message ?: "unknown")
                    lastTickTime = SystemClock.elapsedRealtime()
                }
            }
        }
    }

    when (currentScreen) {
        is Screen.Menu -> MenuScreen(
            highScore = highScore,
            currentTheme = selectedTheme,
            onStartClick = {
                gameState = engine.startNewGame(highScore, currentTheme = selectedTheme)
                ActionLogger.logGameStart(0, highScore)
                if (musicEnabled) soundManager.startBGMusic()
                currentScreen = Screen.Game
            },
            onTutorialClick = {
                gameState = engine.startTutorial(highScore)
                ActionLogger.logTutorialStart()
                currentScreen = Screen.Game
            },
            onThemeChange = { theme ->
                selectedTheme = theme
                saveTheme(context, theme)
            },
            onSettingsClick = {
                currentScreen = Screen.Settings
            }
        )

        is Screen.Game -> GameScreen(
            gameState = gameState,
            onTileTap = { row, col ->
                try {
                    val tile = gameState.tiles.getOrNull(row)?.getOrNull(col)
                    val (newState, result) = engine.onTap(gameState, row, col)
                    gameState = newState
                    when (result) {
                        is TapResult.Correct -> {
                            ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber - 1, true, gameState.score, gameState.timeRemaining)
                            if (soundEnabled) {
                                soundManager.playSuccess(result.combo)
                                if (gameState.score % 10 == 0 && gameState.score > 0) soundManager.playMilestone()
                            }
                        }
                        is TapResult.Wrong -> {
                            ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber, false, gameState.score, gameState.timeRemaining)
                            if (soundEnabled) soundManager.playFailure()
                        }
                        is TapResult.Invalid -> {}
                    }
                } catch (e: Exception) {
                    ActionLogger.logError("tile_tap", e.message ?: "unknown")
                }
            },
            onPauseClick = {
                if (gameState.isPaused) {
                    gameState = engine.resume(gameState)
                    lastTickTime = SystemClock.elapsedRealtime()
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
            currentTheme = selectedTheme,
            onPlayAgain = {
                gameState = engine.startNewGame(highScore, currentTheme = selectedTheme)
                ActionLogger.logGameStart(0, highScore)
                if (musicEnabled) soundManager.startBGMusic()
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
                lastTickTime = SystemClock.elapsedRealtime()
                if (musicEnabled) soundManager.startBGMusic()
                currentScreen = Screen.Game
            }
        )

        is Screen.Settings -> SettingsScreen(
            currentTheme = selectedTheme,
            soundEnabled = soundEnabled,
            musicEnabled = musicEnabled,
            onThemeChange = { theme ->
                selectedTheme = theme
                saveTheme(context, theme)
            },
            onSoundToggle = { enabled ->
                soundEnabled = enabled
                saveSoundEnabled(context, enabled)
            },
            onMusicToggle = { enabled ->
                musicEnabled = enabled
                saveMusicEnabled(context, enabled)
            },
            onResetHighScore = {
                highScore = 0
                saveHighScore(context, 0)
            },
            onBack = {
                currentScreen = Screen.Menu
            }
        )
    }
}

private fun shareScore(context: Context, score: Int, highScore: Int) {
    val text = context.getString(R.string.share_text, score, highScore)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser)))
}
