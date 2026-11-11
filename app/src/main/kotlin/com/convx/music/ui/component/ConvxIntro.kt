/**
 * Aurosly Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import com.convx.music.R
import com.convx.music.ui.ios.compat.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Cold-start intro: the paw icon slides in from the left, then "Aurora" follows, each with a fade.
 * Skipped when reduced motion is on.
 *
 * Smoothness rules this file follows:
 *  - Alpha and slide are applied inside [graphicsLayer], so each frame only redraws the layer. The old
 *    `Modifier.alpha(value)` / `Modifier.offset(value.dp)` read the animation in composition and
 *    recomposed the whole intro on every frame.
 *  - The app underneath is NOT composed while the logo slides in. [onMountApp] fires once the logo is
 *    fully shown; the app composes then, behind the opaque black, and the fade-out waits for that heavy
 *    first frame so the animation never stalls halfway through.
 */
@Composable
fun ConvxIntro(onMountApp: () -> Unit, onFinished: () -> Unit) {
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val mountApp by rememberUpdatedState(onMountApp)
    val finished by rememberUpdatedState(onFinished)

    val iconAlpha = remember { Animatable(0f) }
    val iconOffsetX = remember { Animatable(-60f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetX = remember { Animatable(-60f) }
    val rootAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        if (reducedMotion) {
            mountApp()
            finished()
            return@LaunchedEffect
        }

        val slide = tween<Float>(durationMillis = 420, easing = FastOutSlowInEasing)

        // Icon slides in.
        launch { iconAlpha.animateTo(1f, slide) }
        launch { iconOffsetX.animateTo(0f, slide) }

        // Text follows shortly after.
        delay(180L)
        launch { textAlpha.animateTo(1f, slide) }
        launch { textOffsetX.animateTo(0f, slide) }

        // Let the slide finish and hold briefly, with nothing else competing for the main thread.
        delay(420L + 500L)

        // Compose the app behind the opaque intro, and wait until its first (heavy) frame is drawn.
        mountApp()
        withFrameNanos { }
        withFrameNanos { }

        rootAlpha.animateTo(0f, tween(durationMillis = 300, easing = FastOutSlowInEasing))
        finished()
    }

    if (reducedMotion) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = rootAlpha.value }
            .background(Color.Black)
            // Swallow all touches during the intro.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_launcher_mono),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        alpha = iconAlpha.value
                        translationX = iconOffsetX.value * density
                    },
                colorFilter = ColorFilter.tint(Color.White)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Aurora",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationX = textOffsetX.value * density
                }
            )
        }
    }
}
