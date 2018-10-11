package net.jspiner.epub_viewer.ui.etc

import android.databinding.ViewDataBinding
import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.ui.base.BaseActivity
import net.jspiner.epub_viewer.ui.base.BaseViewModel

class EtcActivity : BaseActivity<ViewDataBinding, BaseViewModel>() {

    override fun getLayoutId() = R.layout.activity_etc

    override fun createViewModel() = EtcViewModel()

    override fun loadState(bundle: Bundle) {
        //no-op
    }

    override fun saveState(bundle: Bundle) {
        //no-op
    }
}