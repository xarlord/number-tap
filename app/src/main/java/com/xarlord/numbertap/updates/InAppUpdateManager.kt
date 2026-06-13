package com.xarlord.numbertap.updates

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType

/**
 * In-App Update manager using Google Play Core library.
 *
 * Checks for available updates when the app starts and shows a flexible
 * update prompt (download in background → prompt to install).
 *
 * Issue #203: Notify users when a new Play Store version is available.
 */
class InAppUpdateManager(
    private val activity: Activity
) {
    companion object {
        private const val TAG = "NumberTap:Update"
        private const val UPDATE_REQUEST_CODE = 7777
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    /**
     * Check for updates. If an update is available, start a flexible update
     * (downloads in background, shows consent dialog).
     */
    fun checkForUpdate() {
        Log.d(TAG, "Checking for app updates...")
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // Pure decision logic (testable, issue #201)
            val decision = resolveUpdateDecision(
                appUpdateInfo.updateAvailability(),
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            )
            when (decision) {
                UpdateDecision.START_FLEXIBLE_UPDATE -> {
                    Log.d(TAG, "Update available! Version code: ${appUpdateInfo.availableVersionCode()}")
                    startFlexibleUpdate(appUpdateInfo)
                }
                UpdateDecision.RESUME_UPDATE -> {
                    Log.d(TAG, "Update already in progress, resuming...")
                    resumeUpdate(appUpdateInfo)
                }
                UpdateDecision.NO_ACTION -> {
                    Log.d(TAG, "No update available (availability=${appUpdateInfo.updateAvailability()})")
                }
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Update check failed: ${e.message}")
        }
    }

    /**
     * Start a flexible update — shows a Play UI dialog asking the user
     * to download and install the update. Downloads happen in background.
     */
    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                UPDATE_REQUEST_CODE
            )
            Log.d(TAG, "Flexible update flow started")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start update flow: ${e.message}")
        }
    }

    /**
     * Resume an update that was already in progress.
     */
    private fun resumeUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                UPDATE_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resume update: ${e.message}")
        }
    }

    /**
     * Call from onResume to check if a flexible update was downloaded
     * and is ready to install. If so, show the "Install" prompt.
     */
    fun checkForPendingInstall() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // Pure decision logic (testable, issue #201)
            if (shouldShowInstallPrompt(appUpdateInfo.installStatus())) {
                Log.d(TAG, "Update downloaded — prompting user to install")
                showInstallPrompt()
            }
        }
    }

    /**
     * Show the "Update downloaded — restart to install" prompt.
     */
    private fun showInstallPrompt() {
        appUpdateManager.completeUpdate()
        Log.d(TAG, "Install prompt shown")
    }

    /**
     * Call from onDestroy to clean up listeners.
     */
    fun cleanup() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            // Pure decision logic (testable, issue #201)
            if (shouldShowInstallPrompt(info.installStatus())) {
                appUpdateManager.completeUpdate()
            }
        }
    }
}
