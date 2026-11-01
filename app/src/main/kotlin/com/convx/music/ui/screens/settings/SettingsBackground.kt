/**
 * Aurora Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.convx.music.constants.SettingsBackgroundKey
import com.convx.music.utils.rememberPreference

/** Gradient backdrops for the settings pages. The glass cards reflect whichever one is picked. */
enum class SettingsBackgroundPreset(val key: String, val colors: List<Color>) {
    AURORA("aurora", listOf(Color(0xFF0B3B2E), Color(0xFF0E5A6B), Color(0xFF1B1F3B))),
    OCEAN("ocean", listOf(Color(0xFF0A2A5C), Color(0xFF1C5FA8), Color(0xFF0B1630))),
    SUNSET("sunset", listOf(Color(0xFF5A1F3D), Color(0xFFB4502E), Color(0xFF1A0F14))),
    VIOLET("violet", listOf(Color(0xFF2E1A5C), Color(0xFF6A3FB5), Color(0xFF120B24))),
    GRAPHITE("graphite", listOf(Color(0xFF2A2C31), Color(0xFF15161A), Color(0xFF0A0A0C))),
    PLAIN("plain", emptyList());

    fun brush(): Brush? = if (colors.isEmpty()) null else Brush.verticalGradient(colors)

    companion object {
        fun fromKey(key: String?): SettingsBackgroundPreset =
            values().firstOrNull { it.key == key } ?: AURORA
    }
}

/** Row of round swatches on the settings root page. Tapping one changes the background everywhere in Settings. */
@Composable
fun SettingsBackgroundPicker() {
    val (selectedKey, setSelectedKey) = rememberPreference(
        SettingsBackgroundKey,
        defaultValue = SettingsBackgroundPreset.AURORA.key,
    )
    val selected = SettingsBackgroundPreset.fromKey(selectedKey)
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsBackgroundPreset.values().forEach { preset ->
            val isSelected = preset == selected
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(preset.brush() ?: Brush.verticalGradient(listOf(Color.Black, Color.Black)))
                    .border(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                        shape = CircleShape,
                    )
                    .clickable { setSelectedKey(preset.key) },
            )
        }
    }
}
