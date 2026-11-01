/**
 * Aurosly Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import com.convx.music.R
import com.convx.music.ui.ios.compat.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Cold-start intro: paw icon slides in from the left, then "Aurora" text
 * slides in from the left slightly after — each with a smooth fade.
 * No snake, no zoom, no haptics. Skipped when reduced motion is on.
 */
@Composable
fun ConvxIntro(onFinished: () -> Unit) {
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }

    // Animatable values for icon (element 1)
    val iconAlpha    = remember { Animatable(0f) }
    val iconOffsetX  = remember { Animatable(-60f) }

    // Animatable values for text (element 2)
    val textAlpha    = remember { Animatable(0f) }
    val textOffsetX  = remember { Animatable(-60f) }

    LaunchedEffect(Unit) {
        if (reducedMotion) {
            onFinished()
            return@LaunchedEffect
        }

        // --- Icon slides in ---
        launch {
            iconAlpha.animateTo(
                1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
        launch {
            iconOffsetX.animateTo(
                0f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }

        // Short stagger: text appears 180ms after icon starts
        delay(180L)

        // --- Text slides in ---
        launch {
            textAlpha.animateTo(
                1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textOffsetX.animateTo(
                0f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }

        // Hold for a moment, then fade everything out
        delay(900L)

        launch {
            iconAlpha.animateTo(
                0f,
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textAlpha.animateTo(
                0f,
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
            )
        }

        delay(360L)
        onFinished()
    }

    if (reducedMotion) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Swallow all touches during the intro
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
            // --- Paw icon ---
            Box(
                modifier = Modifier
                    .alpha(iconAlpha.value)
                    .offset(x = iconOffsetX.value.dp)
            ) {
                val pawVector: ImageVector = ImageVector.vectorResource(R.drawable.ic_launcher_mono)
                Image(
                    imageVector = pawVector,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // --- "Aurora" text ---
            Box(
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(x = textOffsetX.value.dp)
            ) {
                Text(
                    text = "Aurora",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
