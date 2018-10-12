package net.jspiner.epub_viewer.ui.etc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityEtcBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity

class EtcActivity : BaseActivity<ActivityEtcBinding, EtcViewModel>() {

    companion object {
        val REQUEST_CODE = 1111

        val IS_SCROLL_MODE = "keyIsScrollMode"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    private fun init() {
        binding.typeVertical!!.root.setOnClickListener { onTypeChangeClicked() }
        binding.typeHorizontal!!.root.setOnClickListener { onTypeChangeClicked() }
    }

    private fun onTypeChangeClicked() {
        binding.typeVertical!!.apply { selected = !(selected ?: true) }
        binding.typeHorizontal!!.apply { selected = !(selected ?: false) }
    }

    override fun finish() {
        val intent = Intent()
        intent.putExtra(IS_SCROLL_MODE, binding.typeVertical!!.selected)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }
}