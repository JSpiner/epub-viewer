package net.jspiner.epub_viewer.ui.reader.viewer

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewEpubViewerBinding
import net.jspiner.epub_viewer.ui.base.BaseView
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import java.io.File

class EpubView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewEpubViewerBinding, ReaderViewModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_epub_viewer

    init {
        subscribe()

        val adapter = EpubPagerAdapter( (getContext() as AppCompatActivity).supportFragmentManager )
        binding.verticalViewPager.adapter = adapter
    }

    private fun subscribe() {
        viewModel.getCurrentSpineItem()
            .map { viewModel.toManifestItem(it) }
            .subscribe { setSpineFile(it) }
    }

    private fun setSpineFile(file: File) {

    }

}