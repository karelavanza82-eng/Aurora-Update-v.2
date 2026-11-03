package com.convx.music.island

import androidx.media3.exoplayer.ExoPlayer

/** Ukuran dan posisi pill, dibaca dari pengaturan Dynamic Island. */
data class IslandStyle(
    val widthDp: Int = 220,
    val heightDp: Int = 40,
    val offsetDp: Int = 36,
    val offsetXDp: Int = 0,
    val textSp: Int = 13,
    val opacity: Float = 1f,
)

/** Jembatan state antara MusicService, layar pengaturan, dan layanan aksesibilitas. */
object MusicIslandBridge {
    @Volatile var playerProvider: (() -> ExoPlayer)? = null
    @Volatile var enabled: Boolean = false
    /** Aktif saat layar pengaturan Dynamic Island dibuka: pill contoh tampil supaya bisa diatur. */
    @Volatile var preview: Boolean = false
    @Volatile var style: IslandStyle = IslandStyle()
}
