package net.jspiner.epub_viewer.ui.reader.toolbox

import android.content.Context
import android.util.AttributeSet
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewToolboxBinding
import net.jspiner.epub_viewer.ui.base.BaseView

class ToolboxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewToolboxBinding>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_toolbox
}