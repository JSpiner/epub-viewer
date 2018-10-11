package net.jspiner.epub_viewer.ui.etc

import android.app.Activity
import android.content.Intent
import android.databinding.ViewDataBinding
import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.ui.base.BaseActivity
import net.jspiner.epub_viewer.ui.base.BaseViewModel

class EtcActivity : BaseActivity<ViewDataBinding, BaseViewModel>() {

    companion object {
        val REQUEST_CODE = 1111

        fun startActivityForResult(activity: Activity) {
            val intent = Intent(activity, EtcActivity::class.java)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun getLayoutId() = R.layout.activity_etc

    override fun createViewModel() = EtcViewModel()

    override fun loadState(bundle: Bundle) {
        //no-op
    }

    override fun saveState(bundle: Bundle) {
        //no-op
    }
}