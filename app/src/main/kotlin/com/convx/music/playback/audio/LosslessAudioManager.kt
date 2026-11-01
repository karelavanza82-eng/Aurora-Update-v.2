/**
 * Aurosly Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * LosslessAudioManager — detects, tracks, and exposes lossless audio status
 * for the currently playing track.
 *
 * "Lossless" in this context means:
 *   • Local files: FLAC, ALAC (m4a/AAC-lossless), WAV, AIFF, APE, WavPack
 *   • Streams: format bitrate ≥ 320 kbps OR original-format flag is set,
 *              with mime type NOT being lossy-compressed (opus/webm not counted)
 *
 * Usage:
 *   val losslessManager = LosslessAudioManager()
 *   losslessManager.evaluate(mediaItem, format)
 *   if (losslessManager.isLossless) { … }
 */

package com.convx.music.playback.audio

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Quality tier of the currently playing audio. */
enum class AudioQualityTier {
    /** Standard lossy (mp3, aac ≤ 256, opus, etc.) */
    STANDARD,

    /** High bitrate lossy (≥ 320 kbps aac/mp3, 256+ opus – "High Quality") */
    HIGH,

    /**
     * Lossless PCM, FLAC, ALAC, WAV, AIFF, APE, WavPack, or a stream
     * explicitly flagged as "original" quality with no lossy codec.
     */
    LOSSLESS,
}

data class LosslessAudioInfo(
    val tier: AudioQualityTier = AudioQualityTier.STANDARD,
    val formatLabel: String = "",   // e.g. "FLAC 24-bit / 96 kHz", "LOSSLESS 1411 kbps"
    val bitrate: Int = 0,           // bps
    val sampleRate: Int = 0,        // Hz
    val bitDepth: Int = 0,          // 0 = unknown
    val channelCount: Int = 0,
) {
    val isLossless: Boolean get() = tier == AudioQualityTier.LOSSLESS
    val isHighQuality: Boolean get() = tier != AudioQualityTier.STANDARD
}

@OptIn(UnstableApi::class)
class LosslessAudioManager {

    private val _info = MutableStateFlow(LosslessAudioInfo())
    val info: StateFlow<LosslessAudioInfo> = _info.asStateFlow()

    val isLossless: Boolean get() = _info.value.isLossless

    /** Call this whenever ExoPlayer resolves a new Format for the playing track. */
    fun evaluate(mediaItem: MediaItem?, format: Format?) {
        if (format == null) {
            _info.value = LosslessAudioInfo()
            return
        }

        val mimeType = format.sampleMimeType ?: format.containerMimeType ?: ""
        val bitrate = format.bitrate.takeIf { it != Format.NO_VALUE } ?: 0
        val sampleRate = format.sampleRate.takeIf { it != Format.NO_VALUE } ?: 0
        val channels = format.channelCount.takeIf { it != Format.NO_VALUE } ?: 0
        val bitDepth = pcmBitDepth(format.pcmEncoding)

        val localUri = mediaItem?.localConfiguration?.uri
        val tier = when {
            isLosslessMime(mimeType)              -> AudioQualityTier.LOSSLESS
            isLosslessLocal(localUri)             -> AudioQualityTier.LOSSLESS
            isLosslessPcm(format.pcmEncoding)     -> AudioQualityTier.LOSSLESS
            bitrate >= 320_000 && !isLossyMime(mimeType) -> AudioQualityTier.HIGH
            bitrate >= 320_000                    -> AudioQualityTier.HIGH
            else                                  -> AudioQualityTier.STANDARD
        }

        val label = buildFormatLabel(mimeType, bitrate, sampleRate, bitDepth, channels, tier)
        _info.value = LosslessAudioInfo(
            tier = tier,
            formatLabel = label,
            bitrate = bitrate,
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            channelCount = channels,
        )
    }

    /** Reset to unknown (e.g. when playback stops). */
    fun reset() {
        _info.value = LosslessAudioInfo()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun isLosslessMime(mime: String): Boolean {
        val m = mime.lowercase()
        return m.contains("flac") ||
               m.contains("alac") ||
               m.contains("wav")  ||
               m.contains("aiff") ||
               m.contains("ape")  ||
               m.contains("wavpack") ||
               m.contains("x-wav") ||
               m.contains("audio/x-flac")
    }

    private fun isLossyMime(mime: String): Boolean {
        val m = mime.lowercase()
        return m.contains("webm") || m.contains("opus") ||
               m.contains("mp3")  || m.contains("mpeg")
    }

    private fun isLosslessLocal(uri: Uri?): Boolean {
        if (uri == null) return false
        val path = uri.path?.lowercase() ?: return false
        return path.endsWith(".flac")   ||
               path.endsWith(".alac")   ||
               path.endsWith(".wav")    ||
               path.endsWith(".aiff")   ||
               path.endsWith(".aif")    ||
               path.endsWith(".ape")    ||
               path.endsWith(".wv")
    }

    private fun isLosslessPcm(encoding: Int): Boolean = encoding in setOf(
        C.ENCODING_PCM_16BIT,
        C.ENCODING_PCM_24BIT,
        C.ENCODING_PCM_32BIT,
        C.ENCODING_PCM_FLOAT,
        C.ENCODING_PCM_16BIT_BIG_ENDIAN,
        C.ENCODING_PCM_24BIT_BIG_ENDIAN,
        C.ENCODING_PCM_32BIT_BIG_ENDIAN,
    )

    private fun pcmBitDepth(encoding: Int): Int = when (encoding) {
        C.ENCODING_PCM_16BIT, C.ENCODING_PCM_16BIT_BIG_ENDIAN -> 16
        C.ENCODING_PCM_24BIT, C.ENCODING_PCM_24BIT_BIG_ENDIAN -> 24
        C.ENCODING_PCM_32BIT, C.ENCODING_PCM_32BIT_BIG_ENDIAN -> 32
        C.ENCODING_PCM_FLOAT                                   -> 32
        else                                                    -> 0
    }

    private fun buildFormatLabel(
        mime: String,
        bitrate: Int,
        sampleRate: Int,
        bitDepth: Int,
        channels: Int,
        tier: AudioQualityTier,
    ): String {
        val codecName = when {
            mime.contains("flac", true)    -> "FLAC"
            mime.contains("alac", true)    -> "ALAC"
            mime.contains("wav", true)     -> "WAV"
            mime.contains("aiff", true)    -> "AIFF"
            mime.contains("ape", true)     -> "APE"
            mime.contains("wavpack", true) -> "WavPack"
            mime.contains("opus", true)    -> "Opus"
            mime.contains("aac", true)     -> "AAC"
            mime.contains("mp3", true) || mime.contains("mpeg", true) -> "MP3"
            else                           -> mime.substringAfterLast("/").uppercase()
        }

        val parts = mutableListOf<String>()
        if (bitDepth > 0) parts += "$bitDepth-bit"
        if (sampleRate > 0) parts += "${sampleRate / 1000.0}".trimEnd('0').trimEnd('.') + " kHz"
        if (bitrate > 0 && tier != AudioQualityTier.LOSSLESS) {
            parts += "${bitrate / 1000} kbps"
        }
        val suffix = if (parts.isEmpty()) "" else " · ${parts.joinToString(" / ")}"
        return when (tier) {
            AudioQualityTier.LOSSLESS -> "$codecName Lossless$suffix"
            AudioQualityTier.HIGH     -> "$codecName HQ$suffix"
            AudioQualityTier.STANDARD -> "$codecName$suffix"
        }
    }
}
