package net.jspiner.epub_viewer.util

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.widget.ImageView

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("src")
    fun setImageSrc(view: ImageView, image: Drawable) {
        view.setImageDrawable(image)
    }
}