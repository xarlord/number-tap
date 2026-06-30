package com.xarlord.numbertap

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.xarlord.numbertap.ads.AdManagerImpl
import com.xarlord.numbertap.ads.BannerAd
import com.xarlord.numbertap.analytics.AnalyticsTracker
import com.xarlord.numbertap.data.GameState
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.TileState
import com.xarlord.numbertap.game.ActionLogger
import com.xarlord.numbertap.game.GameEngine
import com.xarlord.numbertap.game.TapResult
import com.xarlord.numbertap.audio.SoundManager
import com.xarlord.numbertap.retention.NotificationScheduler
import com.xarlord.numbertap.retention.PlayerProfile
import com.xarlord.numbertap.retention.ProfileRepository
import com.xarlord.numbertap.retention.RetentionLogic
import com.xarlord.numbertap.retention.StreakRewards
import com.xarlord.numbertap.updates.InAppUpdateManager
import com.xarlord.numbertap.ui.GameOverScreen
import com.xarlord.numbertap.ui.GameScreen
import com.xarlord.numbertap.ui.HapticFeedback
import com.xarlord.numbertap.ui.MenuScreen
import com.xarlord.numbertap.ui.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {

    // Reused across lifecycle — avoids creating a new AppUpdateManager each onResume (#201)
    private val updateManager by lazy { InAppUpdateManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Default to portrait but allow rotation (fixes Play Store orientation restriction finding)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        NotificationScheduler.createChannel(this)

        // Initialize AdMob SDK
        val adManager = AdManagerImpl(this)
        adManager.initialize()
        adManager.preloadInterstitial()
        adManager.preloadRewarded()

        // Check for Play Store updates (#203)
        updateManager.checkForUpdate()

        setContent { NumberTapApp(adManager) }
    }

    override fun onResume() {
        super.onResume()
        // Check if a flexible update was downloaded and ready to install
        updateManager.checkForPendingInstall()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up update listeners to prevent memory leaks (#262)
        updateManager.cleanup()
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
private const val KEY_HARD_MODE = "hard_mode"

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

private fun loadHardMode(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_HARD_MODE, false)

private fun saveHardMode(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_HARD_MODE, enabled).apply()
}

@Composable
fun NumberTapApp(adManager: com.xarlord.numbertap.ads.AdManager) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var gameState by remember { mutableStateOf(GameState()) }
    val engine = remember { GameEngine() }
    val context = LocalContext.current
    var highScore by remember { mutableStateOf(loadHighScore(context)) }
    var selectedTheme by remember { mutableStateOf(loadTheme(context)) }
    var soundEnabled by remember { mutableStateOf(loadSoundEnabled(context)) }
    var musicEnabled by remember { mutableStateOf(loadMusicEnabled(context)) }
    var hardMode by remember { mutableStateOf(loadHardMode(context)) }
    var lastTickTime by remember { mutableLongStateOf(0L) }
    var lastCountdownTickSecond by remember { mutableIntStateOf(-1) }
    var lastChaosTickTime by remember { mutableLongStateOf(0L) }

    // --- Retention state ---
    val profileRepository = remember { ProfileRepository(context) }
    var playerProfile by remember { mutableStateOf(profileRepository.loadProfile()) }
    var showDailyLoginPopup by remember { mutableStateOf(false) }
    var dailyLoginCoins by remember { mutableIntStateOf(0) }
    var dailyLoginStreak by remember { mutableIntStateOf(0) }

    // #140: POST_NOTIFICATIONS runtime permission launcher (Android 13+)
    var pendingNotificationEnable by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            playerProfile = playerProfile.copy(notificationEnabled = true)
            profileRepository.saveProfile(playerProfile)
            NotificationScheduler.scheduleStreakReminder(context)
            NotificationScheduler.scheduleMissionsReminder(context)
        }
        pendingNotificationEnable = false
    }

    val soundManager = remember { SoundManager(context) }
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Process daily login on first composition
    LaunchedEffect(Unit) {
        val oldStreak = playerProfile.currentStreak
        val updated = profileRepository.processDailyLogin(playerProfile)
        if (updated.currentStreak != oldStreak || updated.lastLoginDate != playerProfile.lastLoginDate) {
            val coinsAwarded = StreakRewards.coinsForDay(updated.currentStreak)
            playerProfile = updated
            profileRepository.saveProfile(updated)
            dailyLoginCoins = coinsAwarded
            dailyLoginStreak = updated.currentStreak
            showDailyLoginPopup = updated.currentStreak > 0
            AnalyticsTracker.dailyLogin(updated.currentStreak, coinsAwarded)
        }
        // Schedule retention notifications if enabled
        if (playerProfile.notificationEnabled) {
            NotificationScheduler.scheduleStreakReminder(context)
            NotificationScheduler.scheduleMissionsReminder(context)
        }
        AnalyticsTracker.sessionStart()
    }

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

                    // #207: Clear flip animation after duration
                    if (gameState.animatingTileId != null) {
                        delay(com.xarlord.numbertap.data.GameConfig.TILE_FLIP_DURATION_MS.toLong())
                        gameState = engine.clearAnimatingTile(gameState)
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

                        // Chaos mode: flip tiles every ~1 second (#187)
                        if (now - lastChaosTickTime >= 1000L) {
                            gameState = engine.chaosTick(gameState)
                            lastChaosTickTime = now
                        }

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
                            AnalyticsTracker.gameOver(gameState.score, gameState.highScore, gameState.timeRemaining)

                            // Update mission progress after game over
                            val updatedMissions = RetentionLogic.updateMissionProgress(
                                missions = playerProfile.todayMissions,
                                gameScore = gameState.score,
                                maxCombo = gameState.maxCombo,
                                gamesPlayed = 1,
                                correctTaps = gameState.correctTaps
                            )
                            playerProfile = playerProfile.copy(
                                todayMissions = updatedMissions,
                                totalGamesPlayed = playerProfile.totalGamesPlayed + 1,
                                totalCorrectTaps = playerProfile.totalCorrectTaps + gameState.correctTaps,
                                highScore = maxOf(playerProfile.highScore, gameState.highScore)
                            )
                            profileRepository.saveProfile(playerProfile)

                            // Near-achievement notification
                            if (playerProfile.notificationEnabled) {
                                NotificationScheduler.showNearAchievementNotification(context, gameState.score, highScore)
                            }

                            if (soundEnabled) soundManager.playGameOver()
                            soundManager.stopBGMusic()
                            HapticFeedback.gameOverBuzz(context)

                            // Show interstitial ad every N game overs
                            if (adManager is AdManagerImpl && context is Activity) {
                                adManager.showInterstitial(context)
                            }

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

    // Daily login reward popup
    if (showDailyLoginPopup) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDailyLoginPopup = false },
            title = {
                androidx.compose.material3.Text(
                    text = context.getString(R.string.daily_login_popup_title)
                )
            },
            text = {
                androidx.compose.material3.Text(
                    text = context.getString(R.string.daily_login_popup_body, dailyLoginCoins, dailyLoginStreak)
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showDailyLoginPopup = false }) {
                    androidx.compose.material3.Text(
                        text = context.getString(R.string.daily_login_popup_dismiss)
                    )
                }
            }
        )
    }

    // Layout: Column with content on top (respects status bar inset) + banner ad at bottom.
    // This prevents content from being hidden behind camera island and banner from being covered.
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            when (currentScreen) {
                is Screen.Menu -> {
                    MenuScreen(
                    highScore = highScore,
                    currentTheme = selectedTheme,
                    isHardMode = hardMode,
                    coins = playerProfile.coins,
                    streak = playerProfile.currentStreak,
                    onStartClick = {
                        gameState = engine.startNewGame(highScore, currentTheme = selectedTheme, isHardMode = hardMode)
                        ActionLogger.logGameStart(0, highScore)
                        AnalyticsTracker.gameStart(score = 0, highScore = highScore)
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
                    },
                    onHardModeToggle = { enabled ->
                        hardMode = enabled
                        saveHardMode(context, enabled)
                    }
                )
                }

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
                                    AnalyticsTracker.tapCorrect(score = gameState.score, combo = result.combo)

                                    // Phase 3.5: Haptic feedback
                                    if (result.combo >= 3) HapticFeedback.comboBuzz(context)
                                    else HapticFeedback.lightClick(context)

                                    // Award coins for correct tap
                                    playerProfile = playerProfile.copy(
                                        coins = RetentionLogic.awardTapCoins(playerProfile.coins)
                                    )

                                    // Award combo bonus
                                    if (result.combo >= 3) {
                                        playerProfile = playerProfile.copy(
                                            coins = RetentionLogic.awardComboBonus(playerProfile.coins, result.combo)
                                        )
                                    }

                                    // Track total correct taps
                                    playerProfile = playerProfile.copy(
                                        totalCorrectTaps = playerProfile.totalCorrectTaps + 1
                                    )

                                    // #139: Profile saved at game-over only, not every tap

                                    if (soundEnabled) {
                                        soundManager.playSuccess(result.combo)
                                        if (gameState.score % 10 == 0 && gameState.score > 0) {
                                            soundManager.playMilestone()
                                            HapticFeedback.mediumClick(context)
                                            AnalyticsTracker.milestone(gameState.score)
                                        }
                                    }
                                }
                                is TapResult.Wrong -> {
                                    ActionLogger.logTap(row, col, tile?.currentValue ?: -1, gameState.targetNumber, false, gameState.score, gameState.timeRemaining)
                                    AnalyticsTracker.tapWrong(score = gameState.score)
                                    // Phase 3.5: Haptic feedback for wrong tap
                                    HapticFeedback.errorBuzz(context)
                                    if (soundEnabled) {
                                        soundManager.playFailure()
                                        if (gameState.comboCount > 1) soundManager.playComboBreak()
                                    }
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
                    },
                    onMenuClick = {
                        soundManager.stopBGMusic()
                        currentScreen = Screen.Menu
                    }
                )

                is Screen.GameOver -> GameOverScreen(
                    score = gameState.score,
                    highScore = gameState.highScore,
                    isNewHighScore = gameState.isNewHighScore,
                    isReviveEligible = engine.isReviveEligible(gameState),
                    currentTheme = selectedTheme,
                    coinBalance = playerProfile.coins,
                    onPlayAgain = {
                        gameState = engine.startNewGame(highScore, currentTheme = selectedTheme, isHardMode = hardMode)
                        ActionLogger.logGameStart(0, highScore)
                        AnalyticsTracker.gameStart(score = 0, highScore = highScore)
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
                        // Show rewarded ad for revive — issue #16
                        if (adManager is AdManagerImpl && context is Activity) {
                            adManager.showRewardedWithCallbacks(
                                activity = context,
                                onReward = {
                                    gameState = engine.revive(gameState)
                                    lastTickTime = SystemClock.elapsedRealtime()
                                    if (musicEnabled) soundManager.startBGMusic()
                                    currentScreen = Screen.Game
                                    ActionLogger.logRevive(gameState.score, gameState.timeRemaining)
                                },
                                onFailure = {
                                    ActionLogger.logError("revive_failed", "Ad not ready")
                                }
                            )
                        } else {
                            // Stub fallback — just revive without ad
                            gameState = engine.revive(gameState)
                            lastTickTime = SystemClock.elapsedRealtime()
                            if (musicEnabled) soundManager.startBGMusic()
                            currentScreen = Screen.Game
                        }
                    },
                    onSpendCoins = {
                        // #206/#211: Spend coins for revive via the tested repository method
                        val cost = com.xarlord.numbertap.data.GameConfig.COIN_COST_FOR_REVIVE
                        val updated = profileRepository.purchaseRevive(playerProfile, cost)
                        if (updated != null) {
                            playerProfile = updated
                            profileRepository.saveProfile(playerProfile)
                            gameState = engine.revive(gameState)
                            lastTickTime = SystemClock.elapsedRealtime()
                            if (musicEnabled) soundManager.startBGMusic()
                            currentScreen = Screen.Game
                            ActionLogger.logRevive(gameState.score, gameState.timeRemaining)
                        }
                    }
                )

                is Screen.Settings -> SettingsScreen(
                    currentTheme = selectedTheme,
                    soundEnabled = soundEnabled,
                    musicEnabled = musicEnabled,
                    notificationsEnabled = playerProfile.notificationEnabled,
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
                    onNotificationsToggle = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // #140: Request runtime permission on Android 13+
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                playerProfile = playerProfile.copy(notificationEnabled = true)
                                profileRepository.saveProfile(playerProfile)
                                NotificationScheduler.scheduleStreakReminder(context)
                                NotificationScheduler.scheduleMissionsReminder(context)
                            } else {
                                pendingNotificationEnable = true
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            // Below Android 13 or disabling — no permission needed
                            playerProfile = playerProfile.copy(notificationEnabled = enabled)
                            profileRepository.saveProfile(playerProfile)
                            if (enabled) {
                                NotificationScheduler.scheduleStreakReminder(context)
                                NotificationScheduler.scheduleMissionsReminder(context)
                            } else {
                                NotificationScheduler.cancelAll(context)
                            }
                        }
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
        } // end Box (content area)
        // Banner ad at bottom — always visible across ALL screens
        BannerAd()
    } // end Column
}

private fun shareScore(context: Context, score: Int, highScore: Int) {
    val text = context.getString(R.string.share_text, score, highScore)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser)))
}
