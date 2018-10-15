package net.jspiner.epub_viewer.ui.library

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityMainBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity

class LibraryActivity : BaseActivity<ActivityMainBinding, LibraryViewModel>() {

    private val adapter = LibraryAdapter()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun createViewModel(): LibraryViewModel {
        return LibraryViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun loadState(bundle: Bundle) {
        //no-op
    }

    override fun saveState(bundle: Bundle) {
        //no-op
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@LibraryActivity.adapter
        }
    }


}
