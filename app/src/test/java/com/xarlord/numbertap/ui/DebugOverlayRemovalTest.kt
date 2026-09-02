package com.xarlord.numbertap.ui

import org.junit.Assert.assertThrows
import org.junit.Test

/** Regression coverage for issue #225: the unreferenced debug overlay must not ship. */
class DebugOverlayRemovalTest {

    @Test
    fun `debug overlay file facade is absent from production artifacts`() {
        assertThrows(ClassNotFoundException::class.java) {
            Class.forName("com.xarlord.numbertap.ui.DebugOverlayKt")
        }
    }
}
