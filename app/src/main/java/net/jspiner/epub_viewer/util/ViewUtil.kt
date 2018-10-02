package net.jspiner.epub_viewer.util

import android.view.View
import android.view.ViewTreeObserver

fun View.onLayoutLoaded(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(
        object :ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback()
            }
        }
    )
}