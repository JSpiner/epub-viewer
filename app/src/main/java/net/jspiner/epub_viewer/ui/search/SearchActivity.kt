package net.jspiner.epub_viewer.ui.search

import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivitySearchBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity

class SearchActivity: BaseActivity<ActivitySearchBinding, SearchViewModel>() {

    override fun getLayoutId() = R.layout.activity_search

    override fun createViewModel() = SearchViewModel()

    override fun loadState(bundle: Bundle) {
        // no-op
    }

    override fun saveState(bundle: Bundle) {
        // no-op
    }
}