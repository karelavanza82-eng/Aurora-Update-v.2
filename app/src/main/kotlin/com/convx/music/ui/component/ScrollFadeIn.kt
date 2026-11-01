package com.convx.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private const val FADE_IN_MS = 320
private const val START_ALPHA = 0f
private const val START_SCALE = 0.96f

/**
 * Fades and lifts a row in each time it enters composition. Lazy lists dispose rows
 * that leave the viewport, so the effect replays on every scroll in either direction.
 * Values are read in the draw phase only, so animating never recomposes the row.
 */
fun Modifier.scrollFadeIn(): Modifier = composed {
    val progress = remember { Animatable(0f) }
    val offsetPx = with(LocalDensity.current) { 18.dp.toPx() }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(FADE_IN_MS, easing = FastOutSlowInEasing))
    }
    graphicsLayer {
        val p = progress.value
        alpha = START_ALPHA + (1f - START_ALPHA) * p
        translationY = (1f - p) * offsetPx
        val s = START_SCALE + (1f - START_SCALE) * p
        scaleX = s
        scaleY = s
    }
}
