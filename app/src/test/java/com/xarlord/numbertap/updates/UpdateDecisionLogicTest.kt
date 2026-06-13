package com.xarlord.numbertap.updates

import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the pure update-decision logic extracted from
 * InAppUpdateManager.
 *
 * Issue #201: missing tests for InAppUpdateManager.kt.
 */
class UpdateDecisionLogicTest {

    // --- resolveUpdateDecision() ---

    @Test
    fun updateAvailable_andFlexibleAllowed_startsFlexibleUpdate() {
        val decision = resolveUpdateDecision(
            UpdateAvailability.UPDATE_AVAILABLE,
            isFlexibleAllowed = true
        )
        assertEquals(UpdateDecision.START_FLEXIBLE_UPDATE, decision)
    }

    @Test
    fun updateAvailable_andFlexibleNotAllowed_takesNoAction() {
        val decision = resolveUpdateDecision(
            UpdateAvailability.UPDATE_AVAILABLE,
            isFlexibleAllowed = false
        )
        assertEquals(UpdateDecision.NO_ACTION, decision)
    }

    @Test
    fun developerTriggeredUpdateInProgress_resumesUpdate() {
        val decision = resolveUpdateDecision(
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
            isFlexibleAllowed = false // should be ignored
        )
        assertEquals(UpdateDecision.RESUME_UPDATE, decision)
    }

    @Test
    fun noUpdateAvailable_takesNoAction() {
        val decision = resolveUpdateDecision(
            UpdateAvailability.UPDATE_NOT_AVAILABLE,
            isFlexibleAllowed = true
        )
        assertEquals(UpdateDecision.NO_ACTION, decision)
    }

    @Test
    fun unknownAvailability_takesNoAction() {
        val decision = resolveUpdateDecision(
            availability = 9999,
            isFlexibleAllowed = true
        )
        assertEquals(UpdateDecision.NO_ACTION, decision)
    }

    @Test
    fun developerTriggeredUpdateInProgress_ignoresFlexibleFlag() {
        // Resume should happen regardless of isFlexibleAllowed, since the
        // update is already in progress.
        val decision = resolveUpdateDecision(
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
            isFlexibleAllowed = true
        )
        assertEquals(UpdateDecision.RESUME_UPDATE, decision)
    }

    // --- shouldShowInstallPrompt() ---

    @Test
    fun downloadedStatus_triggersInstallPrompt() {
        assertEquals(true, shouldShowInstallPrompt(InstallStatus.DOWNLOADED))
    }

    @Test
    fun installingStatus_doesNotTriggerInstallPrompt() {
        assertEquals(false, shouldShowInstallPrompt(InstallStatus.INSTALLING))
    }

    @Test
    fun pendingStatus_doesNotTriggerInstallPrompt() {
        assertEquals(false, shouldShowInstallPrompt(InstallStatus.PENDING))
    }

    @Test
    fun failedStatus_doesNotTriggerInstallPrompt() {
        assertEquals(false, shouldShowInstallPrompt(InstallStatus.FAILED))
    }

    @Test
    fun unknownStatus_doesNotTriggerInstallPrompt() {
        assertEquals(false, shouldShowInstallPrompt(9999))
    }
}
