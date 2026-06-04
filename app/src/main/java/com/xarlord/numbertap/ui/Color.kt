package com.xarlord.numbertap.ui

import androidx.compose.ui.graphics.Color

object GameColors {
    val Background = Color(0xFF121824)
    val TileNormal = Color(0xFF2A3447)
    val TileTarget = Color(0xFFFACC15)
    val Success = Color(0xFF22C55E)
    val SuccessFade = Color(0xFF1E5E3A)
    val Failure = Color(0xFFEF4444)
    val FailureFade = Color(0xFF6B2121)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
    val ComboGlow = Color(0xFFFBBF24)   // Amber glow for combo
    val TimerUrgent = Color(0xFFEF4444)  // Red for low time
    val TimerWarning = Color(0xFFF97316) // Orange for medium time
    val TimerSafe = Color(0xFF22C55E)    // Green for safe time
    val TierEasy = Color(0xFF22C55E)
    val TierMedium = Color(0xFFF97316)
    val TierHard = Color(0xFFEF4444)
    val PauseOverlay = Color(0x99000000)  // Semi-transparent black
    val ReviveGold = Color(0xFFFFD700)

    // Tile value range colors
    val TileRange1 = Color(0xFFE2E8F0) // 1-10 cool white
    val TileRange2 = Color(0xFF93C5FD) // 11-20 blue
    val TileRange3 = Color(0xFFA78BFA) // 21-30 purple
    val TileRange4 = Color(0xFFF472B6) // 31-40 pink
    val TileRange5 = Color(0xFFFCA5A5) // 41+ warm red

    fun tileColorForValue(value: Int): Color = when {
        value <= 10 -> TileRange1
        value <= 20 -> TileRange2
        value <= 30 -> TileRange3
        value <= 40 -> TileRange4
        else -> TileRange5
    }
}
