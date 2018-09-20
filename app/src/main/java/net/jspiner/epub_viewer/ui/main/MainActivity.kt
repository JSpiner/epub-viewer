package net.jspiner.epub_viewer.ui.main

import android.os.Bundle
import io.reactivex.Completable
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityMainBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epub_viewer.ui.reader.startReaderActivity
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun createViewModel(): BaseViewModel {
        return MainViewModel()
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
        Completable.timer(1000, TimeUnit.MILLISECONDS)
            .subscribe { startReaderActivity(this, File("")) }
    }


}
