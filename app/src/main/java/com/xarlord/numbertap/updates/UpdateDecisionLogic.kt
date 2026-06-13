package com.xarlord.numbertap.updates

import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Pure decision logic for in-app update flow, extracted from
 * [InAppUpdateManager] so it can be unit-tested without an Android
 * device or Play Core Task mocking.
 *
 * Issue #201: make update logic testable.
 */

/** What the update manager should do for a given availability state. */
enum class UpdateDecision {
    /** Start a flexible update flow. */
    START_FLEXIBLE_UPDATE,

    /** Resume an update that was already in progress. */
    RESUME_UPDATE,

    /** Nothing to do (no update, or flexible not allowed). */
    NO_ACTION
}

/**
 * Decide what action to take based on the Play Core
 * [UpdateAvailability] value and whether a flexible update is allowed.
 *
 * @param availability one of [UpdateAvailability] constants
 * @param isFlexibleAllowed result of [com.google.android.play.core.appupdate.AppUpdateInfo.isUpdateTypeAllowed]
 */
fun resolveUpdateDecision(
    availability: Int,
    isFlexibleAllowed: Boolean
): UpdateDecision {
    return when (availability) {
        UpdateAvailability.UPDATE_AVAILABLE ->
            if (isFlexibleAllowed) UpdateDecision.START_FLEXIBLE_UPDATE
            else UpdateDecision.NO_ACTION
        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
            UpdateDecision.RESUME_UPDATE
        else ->
            UpdateDecision.NO_ACTION
    }
}

/**
 * Whether an install prompt should be shown, based on the Play Core
 * [InstallStatus] value.
 *
 * @param installStatus one of [InstallStatus] constants
 */
fun shouldShowInstallPrompt(installStatus: Int): Boolean {
    return installStatus == InstallStatus.DOWNLOADED
}
