package com.convx.music.island

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

/**
 * Host overlay Dynamic Island. Layanan ini hanya dipakai untuk menggambar pill
 * musik, tidak membaca isi layar app lain (canRetrieveWindowContent = false).
 */
class MusicIslandAccessibilityService : AccessibilityService() {
    private var overlay: MusicIslandOverlay? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        overlay?.release()
        overlay = MusicIslandOverlay(this, wm).also { it.start() }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Tidak memproses event apa pun
    }

    override fun onInterrupt() {
        // Tidak ada aksi yang perlu dihentikan
    }

    override fun onUnbind(intent: Intent?): Boolean {
        overlay?.release()
        overlay = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        overlay?.release()
        overlay = null
        super.onDestroy()
    }
}
