package net.jspiner.epub_viewer.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewEpubViewerBinding
import net.jspiner.epub_viewer.ui.base.BaseView

class EpubView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewEpubViewerBinding>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_epub_viewer

}