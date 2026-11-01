/**
 * Aurosly Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * DynamicIslandMusicBar — an iOS Dynamic Island-style music bar shown at the
 * top of the screen while music is playing. It sits inside a composable layer
 * drawn over the normal UI; no overlay permission is required.
 *
 * Layout:
 *   ┌──────────────────────────────────────────┐
 *   │  [album art]  Title · Artist  ♪  ►  ‖   │
 *   └──────────────────────────────────────────┘
 *
 * The pill expands with a spring animation when music starts and collapses
 * when playback stops. Tapping it opens the full player screen.
 */

package com.convx.music.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.convx.music.ui.ios.compat.Icon
import com.convx.music.ui.ios.compat.Text

/**
 * Dynamic Island-style music bar.
 *
 * @param isVisible       Whether the bar should be shown.
 * @param title           Currently playing track title.
 * @param artist          Currently playing artist name.
 * @param albumArtUrl     URL / URI for the album artwork (coil-compatible).
 * @param isPlaying       Whether playback is currently active.
 * @param accentColor     Dominant colour extracted from the album art (optional).
 * @param onTogglePlay    Called when the play/pause button is tapped.
 * @param onSkipNext      Called when the skip-next button is tapped.
 * @param onBarClick      Called when the bar itself (not a button) is tapped.
 */
@Composable
fun DynamicIslandMusicBar(
    isVisible: Boolean,
    title: String,
    artist: String,
    albumArtUrl: Any?,
    isPlaying: Boolean,
    accentColor: Color = Color(0xFF1E1E1E),
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onBarClick: () -> Unit,
) {
    // Pill width animates between compact and expanded
    val pillFraction by animateFloatAsState(
        targetValue = if (isVisible) 0.92f else 0.36f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillFraction"
    )

    val pillHeight by animateDpAsState(
        targetValue = if (isVisible) 58.dp else 32.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pillFraction)
                    .height(pillHeight)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = accentColor.copy(alpha = 0.4f),
                        spotColor = accentColor.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF111111),
                                Color(0xFF1A1A1A),
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBarClick
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Album art thumbnail
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2A2A))
                    ) {
                        if (albumArtUrl != null) {
                            AsyncImage(
                                model = albumArtUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }

                    // Title + artist (expand to fill remaining space)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Waveform / music note indicator
                    MusicWaveIndicator(
                        isPlaying = isPlaying,
                        color = Color(0xFF4FF0D2) // aurora teal accent
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    // Play / Pause button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onTogglePlay
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Skip next button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSkipNext
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip next",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Animated waveform bars ────────────────────────────────────────────────────

@Composable
private fun MusicWaveIndicator(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // Three bars that animate height while playing
    val bar1 by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bar1"
    )
    val bar2 by animateFloatAsState(
        targetValue = if (isPlaying) 0.55f else 0.15f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bar2"
    )
    val bar3 by animateFloatAsState(
        targetValue = if (isPlaying) 0.8f else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf(bar1, bar2, bar3).forEach { frac ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((18 * frac).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = 0.85f))
            )
        }
    }
}
