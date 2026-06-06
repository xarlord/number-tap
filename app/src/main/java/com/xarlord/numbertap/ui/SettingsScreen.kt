package com.xarlord.numbertap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarlord.numbertap.R
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.ThemeConfig

@Composable
fun SettingsScreen(
    currentTheme: GameTheme,
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    onThemeChange: (GameTheme) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onMusicToggle: (Boolean) -> Unit,
    onResetHighScore: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ThemeConfig.colorsFor(currentTheme)
    val style = ThemeConfig.styleFor(currentTheme)
    var showResetDialog by remember { mutableStateOf(false) }
    var showResetDone by remember { mutableStateOf(false) }

    // Hoist accessibility strings
    val backDesc = stringResource(R.string.a11y_back)
    val resetDesc = stringResource(R.string.a11y_reset_highscore)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← ${stringResource(R.string.settings_back)}",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontFamily = style.bodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .semantics {
                            contentDescription = backDesc
                            role = Role.Button
                        }
                        .clickable { onBack() }
                        .padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.settings_title),
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.headerFontFamily,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                // Invisible spacer to center the title
                Text(
                    text = "← ${stringResource(R.string.settings_back)}",
                    color = colors.textPrimary.copy(alpha = 0f),
                    fontSize = 16.sp,
                    fontFamily = style.bodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sound Effects toggle
            SettingsToggleRow(
                label = stringResource(R.string.settings_sound),
                enabled = soundEnabled,
                onToggle = onSoundToggle,
                colors = colors,
                style = style,
                a11yLabel = stringResource(R.string.a11y_sound_toggle, if (soundEnabled) "on" else "off")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Background Music toggle
            SettingsToggleRow(
                label = stringResource(R.string.settings_music),
                enabled = musicEnabled,
                onToggle = onMusicToggle,
                colors = colors,
                style = style,
                a11yLabel = stringResource(R.string.a11y_music_toggle, if (musicEnabled) "on" else "off")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Theme selector
            Text(
                text = stringResource(R.string.settings_theme).uppercase(),
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontFamily = style.bodyFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTheme.entries.forEach { theme ->
                    val tc = ThemeConfig.colorsFor(theme)
                    val isSelected = theme == currentTheme
                    val themeSelectDesc = stringResource(R.string.a11y_theme_select, theme.displayName)
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tc.background)
                            .then(
                                if (isSelected) Modifier.border(2.dp, tc.tileTarget, RoundedCornerShape(8.dp))
                                else Modifier.border(1.dp, tc.panelBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            )
                            .semantics {
                                contentDescription = themeSelectDesc
                                role = Role.Button
                            }
                            .clickable { onThemeChange(theme) }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                theme.displayName,
                                color = tc.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Reset High Score button
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.failure.copy(alpha = 0.2f),
                    contentColor = colors.failure
                ),
                shape = RoundedCornerShape(style.tileCornerRadius.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = resetDesc
                        role = Role.Button
                    }
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_highscore),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.bodyFontFamily
                )
            }

            if (showResetDone) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_reset_done),
                    color = colors.success,
                    fontSize = 13.sp,
                    fontFamily = style.bodyFontFamily
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Privacy Policy
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.panelBackground)
                    .border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_privacy),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.bodyFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_privacy_text),
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontFamily = style.bodyFontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Version info
            Text(
                text = stringResource(R.string.settings_version),
                color = colors.textSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = style.bodyFontFamily
            )
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_reset_highscore),
                    fontFamily = style.headerFontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm),
                    fontFamily = style.bodyFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onResetHighScore()
                    showResetDialog = false
                    showResetDone = true
                }) {
                    Text(
                        text = stringResource(R.string.settings_confirm),
                        color = colors.failure,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(
                        text = stringResource(R.string.settings_cancel),
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.panelBackground,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary
        )
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: com.xarlord.numbertap.data.ThemeColors,
    style: com.xarlord.numbertap.data.ThemeStyle,
    a11yLabel: String = label
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.tileBackground.copy(alpha = 0.5f))
            .border(1.dp, colors.panelBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .semantics { contentDescription = a11yLabel }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = 16.sp,
            fontFamily = style.bodyFontFamily
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = colors.tileTarget,
                checkedThumbColor = colors.textTarget,
                uncheckedTrackColor = colors.tileBackground,
                uncheckedThumbColor = colors.textSecondary
            )
        )
    }
}
