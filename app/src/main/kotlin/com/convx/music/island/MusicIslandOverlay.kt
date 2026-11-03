package com.convx.music.island

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.Player

/**
 * Pill Dynamic Island untuk musik. Digambar lewat WindowManager dengan tipe
 * TYPE_ACCESSIBILITY_OVERLAY, jadi butuh layanan aksesibilitas Aurora aktif.
 * Tampil saat musik diputar, atau saat layar pengaturan sedang dibuka (pratinjau).
 */
class MusicIslandOverlay(
    private val context: Context,
    private val wm: WindowManager,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density

    private var root: LinearLayout? = null
    private var titleView: TextView? = null
    private var playButton: TextView? = null
    private var shownLabel: String? = null
    private var shownPlaying: Boolean? = null
    private var appliedStyle: IslandStyle? = null

    private val tick = object : Runnable {
        override fun run() {
            try {
                refresh()
            } catch (t: Throwable) {
                Log.w(TAG, "Update Dynamic Island musik gagal", t)
            }
            handler.postDelayed(this, POLL_MS)
        }
    }

    fun start() {
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    fun release() {
        handler.removeCallbacks(tick)
        removeView()
    }

    private fun refresh() {
        if (!MusicIslandBridge.enabled && !MusicIslandBridge.preview) {
            removeView()
            return
        }

        val player = MusicIslandBridge.playerProvider?.invoke()
        val item = player?.currentMediaItem
        val realTitle = item?.mediaMetadata?.title?.toString()
        val realPlaying = player != null &&
            (player.isPlaying || (player.playWhenReady && player.playbackState == Player.STATE_BUFFERING))
        val realActive = player != null && !realTitle.isNullOrBlank() &&
            (realPlaying || player.playbackState != Player.STATE_IDLE)

        val label: String
        val playing: Boolean
        if (realActive && MusicIslandBridge.enabled) {
            val artist = item?.mediaMetadata?.artist?.toString()
            label = if (artist.isNullOrBlank()) realTitle.orEmpty() else "$realTitle \u2022 $artist"
            playing = realPlaying
        } else if (MusicIslandBridge.preview) {
            label = "Contoh judul lagu \u2022 Nama artis"
            playing = false
        } else {
            removeView()
            return
        }

        if (root != null && appliedStyle != MusicIslandBridge.style) removeView()
        if (root == null) addView()

        if (label != shownLabel) {
            titleView?.text = label
            shownLabel = label
        }
        if (playing != shownPlaying) {
            playButton?.text = if (playing) "\u275A\u275A" else "\u25B6"
            shownPlaying = playing
        }
    }

    private fun addView() {
        val style = MusicIslandBridge.style
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(style.heightDp)
            setPadding(dp(14), dp(6), dp(6), dp(6))
            background = GradientDrawable().apply {
                setColor(Color.argb((style.opacity.coerceIn(0f, 1f) * 255).toInt(), 0, 0, 0))
                cornerRadius = dp(style.heightDp / 2).toFloat()
            }
            elevation = dp(6).toFloat()
        }

        val title = TextView(context).apply {
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSp.toFloat())
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            maxWidth = dp(style.widthDp)
        }

        fun control(glyph: String, size: Float, onTap: (Player) -> Unit) = TextView(context).apply {
            text = glyph
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { MusicIslandBridge.playerProvider?.invoke()?.let(onTap) }
        }

        val prev = control("\u23EE", 13f) { it.seekToPrevious() }
        val play = control("\u25B6", 14f) { if (it.isPlaying) it.pause() else it.play() }
        val next = control("\u23ED", 13f) { it.seekToNext() }

        container.addView(title)
        container.addView(prev)
        container.addView(play)
        container.addView(next)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = dp(style.offsetXDp)
            y = dp(style.offsetDp)
        }

        try {
            wm.addView(container, params)
            root = container
            titleView = title
            playButton = play
            shownLabel = null
            shownPlaying = null
            appliedStyle = style
        } catch (t: Throwable) {
            Log.w(TAG, "Gagal menampilkan Dynamic Island musik", t)
        }
    }

    private fun removeView() {
        val v = root ?: return
        try {
            wm.removeView(v)
        } catch (_: Throwable) {
            // View sudah dilepas sistem
        }
        root = null
        titleView = null
        playButton = null
        shownLabel = null
        shownPlaying = null
        appliedStyle = null
    }

    private fun dp(value: Int): Int = (value * density).toInt()

    private companion object {
        const val TAG = "MusicIsland"
        const val POLL_MS = 500L
    }
}
