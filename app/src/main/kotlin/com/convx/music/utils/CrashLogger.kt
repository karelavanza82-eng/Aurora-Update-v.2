/**
 * Aurora Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.utils

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.util.Date

/**
 * Saves the stack trace of any crash to Downloads/aurora_crash_*.txt, so the log can be
 * read and shared without logcat access. The previous handler still runs afterwards.
 */
object CrashLogger {

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { save(app, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun save(app: Application, error: Throwable) {
        // MediaStore.Downloads writes without storage permission from Android 10 (API 29).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val text = buildString {
            append("Aurora crash at ").append(Date()).append('\n')
            append("Device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
            append(" | Android ").append(Build.VERSION.RELEASE).append('\n')
            append('\n').append(Log.getStackTraceString(error))
        }
        val resolver = app.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "aurora_crash_${System.currentTimeMillis()}.txt")
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
        resolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        val done = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
        resolver.update(uri, done, null, null)
    }
}
