package net.jspiner.epub_viewer.ui.reader.toolbox

import android.content.Context
import android.util.AttributeSet
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewToolboxBinding
import net.jspiner.epub_viewer.ui.base.BaseView
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel

class ToolboxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewToolboxBinding, ReaderViewModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_toolbox
}