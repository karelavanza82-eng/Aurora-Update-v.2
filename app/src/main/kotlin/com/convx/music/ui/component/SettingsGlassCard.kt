/**
 * Aurora Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Card surface for settings.
 *
 * Real liquid glass (blur and reflection of the page gradient) where the device supports it.
 * Elsewhere the card stays translucent with a light rim, so the gradient behind it still shows.
 */
@Composable
fun Modifier.settingsGlassCard(shape: RoundedCornerShape): Modifier =
    if (isGlassAllowed()) {
        this.liquidGlass(config = LocalGlassEffectConfig.current, shape = shape)
    } else {
        this.clip(shape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(0.6.dp, Color.White.copy(alpha = 0.22f), shape)
    }
