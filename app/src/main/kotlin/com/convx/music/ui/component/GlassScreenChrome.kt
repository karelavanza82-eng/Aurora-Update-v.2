/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.convx.music.ui.component.backdrop.backdrops.LayerBackdrop
import com.convx.music.ui.component.backdrop.backdrops.layerBackdrop
import com.convx.music.ui.component.backdrop.backdrops.rememberBackdropFreeze
import com.convx.music.ui.component.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** Provides attached backdrop and Haze wiring for flat screens. Keep [FrostedTopBar] a sibling of the captured scrolling content to avoid self-capture. */
@Stable
class GlassScreenChrome internal constructor(
    val backdrop: LayerBackdrop,
    val hazeState: HazeState,
    private val freeze: com.convx.music.ui.component.backdrop.backdrops.BackdropFreeze,
    /**
     * Holds only the page background, never the content. Glass surfaces inside the content
     * sample this instead of [backdrop], because sampling [backdrop] from inside the content
     * that it records recurses the RenderNode draw.
     */
    val cardBackdrop: LayerBackdrop? = null,
) {
    // Reuse the scrolling content's frozen state to avoid re-running its effects on every scroll frame.
    val frozen: () -> Boolean get() = freeze.frozen
    // Keep the haze source around scrollable content only. If it contains the blur overlay,
    // Haze captures that overlay itself and the blur has no useful source.
    /** Apply to a Box that wraps only the screen's scrolling content. */
    fun screenModifier(): Modifier = Modifier.hazeSource(hazeState)

    /** Apply to the screen's scrolling root (LazyColumn/Column/etc). */
    fun contentModifier(): Modifier = Modifier
        .nestedScroll(freeze.connection)
        // Isolate sibling redraws and flatten list content for the backdrop capture.
        .graphicsLayer()
        .layerBackdrop(backdrop, frozen = freeze.frozen)
        .graphicsLayer()
}

@Composable
fun rememberGlassScreenChrome(
    background: Color,
    backgroundBrush: androidx.compose.ui.graphics.Brush? = null,
    glassOverPageSource: Boolean = false,
): GlassScreenChrome {
    val freeze = rememberBackdropFreeze()
    val backdrop = rememberLayerBackdrop(
        // Fills the screen background into the capture BEFORE content, same
        // reason MainActivity's appBackdrop does: without it the list records
        // onto a transparent canvas and the blur reads as part-transparent /
        // ghosted instead of solid frosted content.
        onDraw = remember(background, backgroundBrush) {
            val bg = background
            { if (backgroundBrush != null) drawRect(backgroundBrush) else drawRect(bg); drawContent() }
        }
    )
    val cardBackdrop = if (glassOverPageSource) rememberLayerBackdrop(
        onDraw = remember(background, backgroundBrush) {
            val bg = background
            { if (backgroundBrush != null) drawRect(backgroundBrush) else drawRect(bg) }
        }
    ) else null
    val hazeState = remember { HazeState() }
    return remember(backdrop, hazeState, freeze, cardBackdrop) { GlassScreenChrome(backdrop, hazeState, freeze, cardBackdrop) }
}
