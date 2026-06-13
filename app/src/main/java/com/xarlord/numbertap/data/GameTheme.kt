package com.xarlord.numbertap.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Visual theme system — each theme defines colors, tile rendering style,
 * fonts, and decorative elements for the entire game.
 */
enum class GameTheme(val displayName: String, val description: String) {
    TERMINAL("Terminal", "Green-on-black CRT terminal"),
    CHALKBOARD("Chalkboard", "Classroom chalkboard"),
    MATRIX("Matrix", "Digital rain"),
    DEFAULT("Default", "Dark minimalist")
}

data class ThemeColors(
    val background: Color,
    val tileBackground: Color,
    val tileTarget: Color,
    val tileTargetGlow: Color,
    val success: Color,
    val successFade: Color,
    val failure: Color,
    val failureFade: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTarget: Color,
    val comboGlow: Color,
    val timerSafe: Color,
    val timerWarning: Color,
    val timerUrgent: Color,
    val timerBarBg: Color,
    val panelBackground: Color,
    val panelBorder: Color,
    val vignetteColor: Color
)

data class ThemeStyle(
    val tileCornerRadius: Float, // dp
    val tileBorderWidth: Float,  // dp
    val tileFontFamily: FontFamily,
    val tileFontSize: TextUnit,
    val headerFontFamily: FontFamily,
    val bodyFontFamily: FontFamily,
    val showTileBorder: Boolean,
    val showScanlines: Boolean,
    val showGridDots: Boolean,
    val tileBorderChar: String,  // For ASCII-style rendering
    val tileCornerChar: String
)

object ThemeConfig {

    fun colorsFor(theme: GameTheme): ThemeColors = when (theme) {
        GameTheme.TERMINAL -> ThemeColors(
            background = Color(0xFF000000),
            tileBackground = Color(0xFF0a1a0a),
            tileTarget = Color(0xFF00FF41),
            tileTargetGlow = Color(0xFF00FF41),
            success = Color(0xFF00FF41),
            successFade = Color(0xFF0a3a0a),
            failure = Color(0xFFFF0000),
            failureFade = Color(0xFF3a0a0a),
            textPrimary = Color(0xFF00FF41),
            textSecondary = Color(0xFF007a20),
            textTarget = Color(0xFF000000),
            comboGlow = Color(0xFF00FF41),
            timerSafe = Color(0xFF00FF41),
            timerWarning = Color(0xFFFFFF00),
            timerUrgent = Color(0xFFFF0000),
            timerBarBg = Color(0xFF0a2a0a),
            panelBackground = Color(0xFF050f05),
            panelBorder = Color(0xFF00FF41),
            vignetteColor = Color(0xFF00FF41)
        )
        GameTheme.CHALKBOARD -> ThemeColors(
            background = Color(0xFF1a3a2a),
            tileBackground = Color(0xFF234a38),
            tileTarget = Color(0xFFFFFDD0),
            tileTargetGlow = Color(0xFFFFFDD0),
            success = Color(0xFF90EE90),
            successFade = Color(0xFF2a5a3a),
            failure = Color(0xFFFF6B6B),
            failureFade = Color(0xFF3a2a2a),
            textPrimary = Color(0xFFFFFDD0),
            textSecondary = Color(0xFFc8b888),
            textTarget = Color(0xFF1a3a2a),
            comboGlow = Color(0xFFFFD700),
            timerSafe = Color(0xFF90EE90),
            timerWarning = Color(0xFFFFD700),
            timerUrgent = Color(0xFFFF6B6B),
            timerBarBg = Color(0xFF0d2a1a),
            panelBackground = Color(0xFF122a1e),
            panelBorder = Color(0xFF5a7a6a),
            vignetteColor = Color(0xFFFF6B6B)
        )
        GameTheme.MATRIX -> ThemeColors(
            background = Color(0xFF000000),
            tileBackground = Color(0xFF001a00),
            tileTarget = Color(0xFF00FF00),
            tileTargetGlow = Color(0xFF88FFAA),
            success = Color(0xFF00FF00),
            successFade = Color(0xFF003a00),
            failure = Color(0xFFFF0000),
            failureFade = Color(0xFF3a0000),
            textPrimary = Color(0xFF00CC00),
            textSecondary = Color(0xFF006600),
            textTarget = Color(0xFF000000),
            comboGlow = Color(0xFF00FF00),
            timerSafe = Color(0xFF00FF00),
            timerWarning = Color(0xFFAAFF00),
            timerUrgent = Color(0xFFFF3300),
            timerBarBg = Color(0xFF001a00),
            panelBackground = Color(0xFF000800),
            panelBorder = Color(0xFF00FF00),
            vignetteColor = Color(0xFFFF0000)
        )
        GameTheme.DEFAULT -> ThemeColors(
            background = Color(0xFF121824),
            tileBackground = Color(0xFF2A3447),
            tileTarget = Color(0xFFFACC15),
            tileTargetGlow = Color(0xFFFACC15),
            success = Color(0xFF22C55E),
            successFade = Color(0xFF1E5E3A),
            failure = Color(0xFFEF4444),
            failureFade = Color(0xFF6B2121),
            textPrimary = Color.White,
            textSecondary = Color(0xFF9CA3AF),
            textTarget = Color(0xFF121824),
            comboGlow = Color(0xFFFBBF24),
            timerSafe = Color(0xFF22C55E),
            timerWarning = Color(0xFFF97316),
            timerUrgent = Color(0xFFEF4444),
            timerBarBg = Color(0xFF2A3447),
            panelBackground = Color(0xFF0e1420),
            panelBorder = Color(0xFF3A4457),
            vignetteColor = Color.Red
        )
    }

    fun styleFor(theme: GameTheme): ThemeStyle = when (theme) {
        GameTheme.TERMINAL -> ThemeStyle(
            tileCornerRadius = 0f,
            tileBorderWidth = 2f,
            tileFontFamily = FontFamily.Monospace,
            tileFontSize = 20.sp,
            headerFontFamily = FontFamily.Monospace,
            bodyFontFamily = FontFamily.Monospace,
            showTileBorder = true,
            showScanlines = true,
            showGridDots = false,
            tileBorderChar = "│",
            tileCornerChar = "┌"
        )
        GameTheme.CHALKBOARD -> ThemeStyle(
            tileCornerRadius = 28f,
            tileBorderWidth = 2f,
            tileFontFamily = FontFamily.Cursive,
            tileFontSize = 22.sp,
            headerFontFamily = FontFamily.Cursive,
            bodyFontFamily = FontFamily.Cursive,
            showTileBorder = true,
            showScanlines = false,
            showGridDots = false,
            tileBorderChar = "~",
            tileCornerChar = "○"
        )
        GameTheme.MATRIX -> ThemeStyle(
            tileCornerRadius = 4f,
            tileBorderWidth = 1f,
            tileFontFamily = FontFamily.Monospace,
            tileFontSize = 20.sp,
            headerFontFamily = FontFamily.Monospace,
            bodyFontFamily = FontFamily.Monospace,
            showTileBorder = true,
            showScanlines = false,
            showGridDots = false,
            tileBorderChar = "║",
            tileCornerChar = "╔"
        )
        GameTheme.DEFAULT -> ThemeStyle(
            tileCornerRadius = 10f,
            tileBorderWidth = 0f,
            tileFontFamily = FontFamily.Default,
            tileFontSize = 24.sp,
            headerFontFamily = FontFamily.Default,
            bodyFontFamily = FontFamily.Default,
            showTileBorder = false,
            showScanlines = false,
            showGridDots = false,
            tileBorderChar = "",
            tileCornerChar = ""
        )
    }
}
