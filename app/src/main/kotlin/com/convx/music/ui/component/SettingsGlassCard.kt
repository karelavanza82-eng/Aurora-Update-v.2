/**
 * Aurora Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Card surface for settings.
 *
 * Glass samples the page gradient only (see GlassScreenChrome.cardBackdrop), never the
 * content it sits in, so it does not recurse the RenderNode draw.
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
