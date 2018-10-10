package net.jspiner.epub_viewer.ui.reader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityReaderBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity
import net.jspiner.epub_viewer.paginator.ScrollPaginator
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
        initViews()

        viewModel.setEpubFile(epubFile)
        requestPermission()
    }

    private fun initViews() {
        binding.toolboxView.touchSender = { binding.epubView.sendTouchEvent(it) }
        setNavigationBarColor(R.color.colorPrimaryDark)
    }

    private fun requestPermission() {
        TedPermission.with(this)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    loadEpub()
                }

                override fun onPermissionDenied(list: MutableList<String>?) {
                    requestPermission()
                }
            }).check()
    }

    private fun loadEpub() {
        viewModel.extractEpub(cacheDir)
            .toSingleDefault(0)
            .flatMap { ScrollPaginator(baseContext, viewModel.extractedEpub).calculatePage() }
            .doOnSuccess { viewModel.setPageInfo(it) }
            .doOnSuccess { viewModel.navigateToSpine(0) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doOnSuccess { hideLoading() }
            .subscribe()
    }
}