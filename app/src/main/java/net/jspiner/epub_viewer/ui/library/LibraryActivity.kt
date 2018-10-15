package net.jspiner.epub_viewer.ui.library

import android.os.Bundle
import android.os.Environment
import io.reactivex.Completable
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityMainBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity
import net.jspiner.epub_viewer.ui.reader.startReaderActivity
import java.io.File
import java.util.concurrent.TimeUnit

class LibraryActivity : BaseActivity<ActivityMainBinding, LibraryViewModel>() {

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
        Completable.timer(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                val dummyPath = Environment.getExternalStorageDirectory().path + "/Download/test.epub"
                startReaderActivity(this, File(dummyPath))
            }
    }


}
