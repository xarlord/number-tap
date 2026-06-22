package com.xarlord.numbertap.retention

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.xarlord.numbertap.MainActivity
import com.xarlord.numbertap.R
import java.util.concurrent.TimeUnit

/**
 * Notification scheduler using WorkManager.
 * Issue #94: Push notification system for retention.
 */
object NotificationScheduler {

    const val CHANNEL_ID = "number_tap_reminders"
    const val CHANNEL_NAME = "Game Reminders"

    // Work tags for cancellation
    private const val WORK_STREAK = "streak_reminder"
    private const val WORK_MISSIONS = "missions_reminder"

    // Near-achievement notification threshold (points away from high score)
    const val NEAR_ACHIEVEMENT_THRESHOLD = 10

    // Notification IDs
    const val NOTIFICATION_ID_NEAR_ACHIEVEMENT = 1001
    const val NOTIFICATION_ID_STREAK = 2001
    const val NOTIFICATION_ID_MISSIONS = 2002

    /**
     * Create notification channel (required Android 8+).
     * Call once in Application.onCreate or Activity.onCreate.
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to keep your streak and complete missions"
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule the streak-at-risk reminder (8pm daily if not opened).
     * Replaces any existing streak reminder.
     */
    fun scheduleStreakReminder(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Schedule daily at ~8pm (20:00)
        // Calculate initial delay to next 8pm
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 20)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= now) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = calendar.timeInMillis - now

        val work = PeriodicWorkRequestBuilder<StreakReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(WORK_STREAK)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_STREAK,
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }

    /**
     * Schedule daily missions ready notification (9am).
     */
    fun scheduleMissionsReminder(context: Context) {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= now) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = calendar.timeInMillis - now

        val work = PeriodicWorkRequestBuilder<MissionsReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_MISSIONS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_MISSIONS,
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }

    /**
     * Cancel all scheduled notifications.
     */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_STREAK)
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_MISSIONS)
    }

    /**
     * Show an immediate "near achievement" notification after game over.
     */
    fun showNearAchievementNotification(context: Context, score: Int, highScore: Int) {
        val pointsAway = highScore - score
        if (pointsAway <= 0 || pointsAway > NEAR_ACHIEVEMENT_THRESHOLD) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_near_achievement_title))
            .setContentText(context.resources.getQuantityString(R.plurals.notif_near_achievement_body, pointsAway, pointsAway))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_NEAR_ACHIEVEMENT, notification)
    }
}

/**
 * WorkManager worker: streak at risk reminder.
 */
class StreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = ProfileRepository(applicationContext)
        val profile = repo.loadProfile()

        // Only notify if player has a streak of 2+ days
        if (profile.currentStreak < 2) return Result.success()

        // Check if already logged in today
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        if (profile.lastLoginDate == today) return Result.success()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notif_streak_title))
            .setContentText(applicationContext.getString(R.string.notif_streak_body, profile.currentStreak))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationScheduler.NOTIFICATION_ID_STREAK, notification)

        return Result.success()
    }
}

/**
 * WorkManager worker: daily missions ready reminder.
 */
class MissionsReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = ProfileRepository(applicationContext)
        val profile = repo.loadProfile()

        val totalCoins = profile.todayMissions.sumOf { it.coinReward }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notif_missions_title))
            .setContentText(applicationContext.resources.getQuantityString(R.plurals.notif_missions_body, totalCoins, totalCoins))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationScheduler.NOTIFICATION_ID_MISSIONS, notification)

        return Result.success()
    }
}
