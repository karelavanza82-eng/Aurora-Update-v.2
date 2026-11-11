/**
 * Aurora Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.utils

import android.app.ActivityManager
import android.content.Context

/**
 * Whether this phone is short on RAM, decided once per process.
 *
 * Android only flags "Go" devices as low-RAM, so an ordinary 3-4 GB phone passes that check and then
 * pays for live backdrop blur, glass shaders and animated mesh backgrounds it cannot afford. This
 * treats anything that reports 4 GiB or less (a "4 GB" phone reports roughly 3.6-3.9 GiB) as low-end.
 *
 * It only changes defaults. A user who has set a preference explicitly keeps their choice.
 */
object DeviceTier {
    private const val LOW_END_MAX_BYTES = 4L * 1024 * 1024 * 1024

    @Volatile
    private var cached: Boolean? = null

    /** Cheap after the first call. Safe to call from any thread. */
    fun isLowEnd(context: Context): Boolean =
        cached ?: compute(context.applicationContext).also { cached = it }

    /** Value for call sites without a Context. False until [isLowEnd] has run once; App.onCreate runs it. */
    val lowEnd: Boolean
        get() = cached ?: false

    private fun compute(context: Context): Boolean {
        val am = context.getSystemService(ActivityManager::class.java) ?: return false
        if (am.isLowRamDevice) return true
        val info = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        return info.totalMem in 1..LOW_END_MAX_BYTES
    }
}
