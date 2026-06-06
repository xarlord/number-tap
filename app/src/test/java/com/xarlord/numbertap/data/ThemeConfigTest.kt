package com.xarlord.numbertap.data

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class GameThemeTest {

    @Test
    fun `all themes have non-empty displayName`() {
        for (theme in GameTheme.entries) {
            assertTrue("Theme $theme should have non-empty displayName", theme.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all themes have non-empty description`() {
        for (theme in GameTheme.entries) {
            assertTrue("Theme $theme should have non-empty description", theme.description.isNotEmpty())
        }
    }

    @Test
    fun `exactly 4 themes exist`() {
        assertEquals(4, GameTheme.entries.size)
    }

    @Test
    fun `themes are TERMINAL CHALKBOARD MATRIX DEFAULT`() {
        val names = GameTheme.entries.map { it.name }
        assertTrue(names.contains("TERMINAL"))
        assertTrue(names.contains("CHALKBOARD"))
        assertTrue(names.contains("MATRIX"))
        assertTrue(names.contains("DEFAULT"))
    }

    @Test
    fun `valueOf returns correct theme for each name`() {
        for (theme in GameTheme.entries) {
            assertEquals(theme, GameTheme.valueOf(theme.name))
        }
    }

    @Test
    fun `DEFAULT is last entry`() {
        assertEquals(GameTheme.DEFAULT, GameTheme.entries.last())
    }
}

class ThemeConfigColorsTest {

    @Test
    fun `each theme returns distinct colors`() {
        val colorSets = GameTheme.entries.map { ThemeConfig.colorsFor(it) }
        for (i in colorSets.indices) {
            for (j in i + 1 until colorSets.size) {
                assertNotEquals("Themes ${GameTheme.entries[i]} and ${GameTheme.entries[j]} should have different colors",
                    colorSets[i], colorSets[j])
            }
        }
    }

    @Test
    fun `DEFAULT background matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFF121824), colors.background)
    }

    @Test
    fun `DEFAULT tile background matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFF2A3447), colors.tileBackground)
    }

    @Test
    fun `DEFAULT tile target matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFFFACC15), colors.tileTarget)
    }

    @Test
    fun `DEFAULT success matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFF22C55E), colors.success)
    }

    @Test
    fun `DEFAULT failure matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFFEF4444), colors.failure)
    }

    @Test
    fun `DEFAULT successFade matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFF1E5E3A), colors.successFade)
    }

    @Test
    fun `DEFAULT failureFade matches GDD spec`() {
        val colors = ThemeConfig.colorsFor(GameTheme.DEFAULT)
        assertEquals(Color(0xFF6B2121), colors.failureFade)
    }

    @Test
    fun `TERMINAL has black background`() {
        val colors = ThemeConfig.colorsFor(GameTheme.TERMINAL)
        assertEquals(Color(0xFF000000), colors.background)
    }

    @Test
    fun `TERMINAL has green primary text`() {
        val colors = ThemeConfig.colorsFor(GameTheme.TERMINAL)
        assertEquals(Color(0xFF00FF41), colors.textPrimary)
    }

    @Test
    fun `MATRIX has black background`() {
        val colors = ThemeConfig.colorsFor(GameTheme.MATRIX)
        assertEquals(Color(0xFF000000), colors.background)
    }

    @Test
    fun `CHALKBOARD has green-ish background`() {
        val colors = ThemeConfig.colorsFor(GameTheme.CHALKBOARD)
        assertEquals(Color(0xFF1a3a2a), colors.background)
    }

    @Test
    fun `all themes have non-zero alpha colors`() {
        for (theme in GameTheme.entries) {
            val colors = ThemeConfig.colorsFor(theme)
            assertTrue("${theme}.background should have alpha", colors.background.alpha > 0f)
            assertTrue("${theme}.tileBackground should have alpha", colors.tileBackground.alpha > 0f)
            assertTrue("${theme}.textPrimary should have alpha", colors.textPrimary.alpha > 0f)
            assertTrue("${theme}.success should have alpha", colors.success.alpha > 0f)
            assertTrue("${theme}.failure should have alpha", colors.failure.alpha > 0f)
            assertTrue("${theme}.tileTarget should have alpha", colors.tileTarget.alpha > 0f)
        }
    }

    @Test
    fun `TERMINAL and MATRIX have distinct backgrounds from CHALKBOARD and DEFAULT`() {
        val bgs = GameTheme.entries.map { ThemeConfig.colorsFor(it).background }
        // TERMINAL and MATRIX both use black, but others are distinct
        assertNotEquals(ThemeConfig.colorsFor(GameTheme.CHALKBOARD).background, ThemeConfig.colorsFor(GameTheme.DEFAULT).background)
        assertNotEquals(ThemeConfig.colorsFor(GameTheme.TERMINAL).background, ThemeConfig.colorsFor(GameTheme.DEFAULT).background)
        assertNotEquals(ThemeConfig.colorsFor(GameTheme.CHALKBOARD).background, ThemeConfig.colorsFor(GameTheme.TERMINAL).background)
    }
}

class ThemeConfigStyleTest {

    @Test
    fun `each theme returns distinct styles`() {
        val styles = GameTheme.entries.map { ThemeConfig.styleFor(it) }
        for (i in styles.indices) {
            for (j in i + 1 until styles.size) {
                assertNotEquals("Themes ${GameTheme.entries[i]} and ${GameTheme.entries[j]} should have different styles",
                    styles[i], styles[j])
            }
        }
    }

    @Test
    fun `TERMINAL uses monospace font`() {
        val style = ThemeConfig.styleFor(GameTheme.TERMINAL)
        assertEquals(androidx.compose.ui.text.font.FontFamily.Monospace, style.tileFontFamily)
    }

    @Test
    fun `MATRIX uses monospace font`() {
        val style = ThemeConfig.styleFor(GameTheme.MATRIX)
        assertEquals(androidx.compose.ui.text.font.FontFamily.Monospace, style.tileFontFamily)
    }

    @Test
    fun `CHALKBOARD uses cursive font`() {
        val style = ThemeConfig.styleFor(GameTheme.CHALKBOARD)
        assertEquals(androidx.compose.ui.text.font.FontFamily.Cursive, style.tileFontFamily)
    }

    @Test
    fun `DEFAULT uses default font`() {
        val style = ThemeConfig.styleFor(GameTheme.DEFAULT)
        assertEquals(androidx.compose.ui.text.font.FontFamily.Default, style.tileFontFamily)
    }

    @Test
    fun `TERMINAL shows scanlines`() {
        val style = ThemeConfig.styleFor(GameTheme.TERMINAL)
        assertTrue(style.showScanlines)
    }

    @Test
    fun `other themes do not show scanlines`() {
        for (theme in GameTheme.entries.filter { it != GameTheme.TERMINAL }) {
            assertFalse("${theme} should not show scanlines", ThemeConfig.styleFor(theme).showScanlines)
        }
    }

    @Test
    fun `TERMINAL has zero corner radius`() {
        val style = ThemeConfig.styleFor(GameTheme.TERMINAL)
        assertEquals(0f, style.tileCornerRadius, 0.01f)
    }

    @Test
    fun `CHALKBOARD has large corner radius`() {
        val style = ThemeConfig.styleFor(GameTheme.CHALKBOARD)
        assertTrue(style.tileCornerRadius > 20f)
    }

    @Test
    fun `DEFAULT has moderate corner radius`() {
        val style = ThemeConfig.styleFor(GameTheme.DEFAULT)
        assertTrue(style.tileCornerRadius > 0f)
    }

    @Test
    fun `TERMINAL and MATRIX show tile borders`() {
        assertTrue(ThemeConfig.styleFor(GameTheme.TERMINAL).showTileBorder)
        assertTrue(ThemeConfig.styleFor(GameTheme.MATRIX).showTileBorder)
    }

    @Test
    fun `DEFAULT does not show tile border`() {
        assertFalse(ThemeConfig.styleFor(GameTheme.DEFAULT).showTileBorder)
    }
}
