package net.jspiner.epub_viewer.ui.reader

import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityReaderBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity

class ReaderActivity : BaseActivity<ActivityReaderBinding>() {

    override fun getLayoutId() = R.layout.activity_reader
    override fun createViewModel() = ReaderViewModel()

    override fun loadState(bundle: Bundle) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveState(bundle: Bundle) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {

    }
}