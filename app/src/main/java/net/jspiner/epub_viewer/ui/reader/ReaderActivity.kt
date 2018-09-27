package net.jspiner.epub_viewer.ui.reader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityReaderBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity
import java.io.File

const val INTENT_KEY_FILE = "intentKeyFile"

fun startReaderActivity(context: Context, epubFile: File) {
    val intent = Intent(context, ReaderActivity::class.java)
    intent.putExtra(INTENT_KEY_FILE, epubFile)
    context.startActivity(intent)
}

class ReaderActivity : BaseActivity<ActivityReaderBinding, ReaderViewModel>() {

    override fun getLayoutId() = R.layout.activity_reader
    override fun createViewModel() = ReaderViewModel()

    private lateinit var epubFile: File

    override fun loadState(bundle: Bundle) {
        epubFile = bundle.getSerializable(INTENT_KEY_FILE) as File
    }

    override fun saveState(bundle: Bundle) {
        bundle.putSerializable(INTENT_KEY_FILE, epubFile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        viewModel.setEpubFile(epubFile)
        TedPermission.with(this)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    viewModel.extractEpub(cacheDir)
                        .subscribe {
                            viewModel.navigateToPoint(
                                viewModel.extractedEpub.toc.navMap.navPoints[0]
                            )
                        }
                }

                override fun onPermissionDenied(list: MutableList<String>?) {
                    init()
                }
            }).check()
    }
}