package net.jspiner.viewer.ui.reader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.jspiner.viewer.R
import net.jspiner.viewer.databinding.ActivityReaderBinding
import net.jspiner.viewer.dto.Epub
import net.jspiner.viewer.dto.ViewerType
import net.jspiner.viewer.paginator.PagePaginator
import net.jspiner.viewer.paginator.Paginator
import net.jspiner.viewer.paginator.ScrollPaginator
import net.jspiner.viewer.ui.base.BaseActivity
import net.jspiner.viewer.ui.etc.EtcActivity
import net.jspiner.viewer.ui.search.SearchActivity
import java.io.File

class ReaderActivity : BaseActivity<ActivityReaderBinding, ReaderViewModel>() {

    companion object {

        const val INTENT_KEY_FILE = "intentKeyFile"

        fun startActivity(context: Context, epubFile: File) {
            val intent = Intent(context, ReaderActivity::class.java)
            intent.putExtra(INTENT_KEY_FILE, epubFile)
            context.startActivity(intent)
        }
    }

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
        viewModel.getViewerType()
            .skip(1)
            .compose(bindLifecycle())
            .subscribe { calculatePage() }

        loadEpub()
    }

    private fun initViews() {
        binding.toolboxView.touchSender = { binding.epubView.sendTouchEvent(it) }
        setNavigationBarColor(R.color.colorPrimaryDark)
    }

    private fun loadEpub() {
        viewModel.extractEpub(cacheDir)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doOnComplete { hideLoading() }
            .doOnComplete { calculatePage() }
            .compose(bindLifecycle<Any>())
            .subscribe()
    }

    private fun calculatePage() {
        getPaginator(baseContext, viewModel.extractedEpub).calculatePage()
            .doOnSuccess { viewModel.setPageInfo(it) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doOnSuccess { hideLoading() }
            .compose(bindLifecycle())
            .subscribe()
    }

    private fun getPaginator(context: Context, epub: Epub): Paginator {
        return when (viewModel.getCurrentViewerType()!!) {
            ViewerType.SCROLL -> ScrollPaginator(context, epub)
            ViewerType.PAGE -> PagePaginator(context, epub)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EtcActivity.REQUEST_CODE -> onEtcActivityResult(resultCode, data!!)
            SearchActivity.REQUEST_CODE -> onSearchActivityResult(resultCode, data)
            else -> RuntimeException("대응하지 못한 requestCode : $requestCode")
        }
    }

    private fun onEtcActivityResult(resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val isScrollMode = data.getBooleanExtra(EtcActivity.IS_SCROLL_MODE, true)

        if (isScrollMode) {
            viewModel.setViewerType(ViewerType.SCROLL)
        } else {
            viewModel.setViewerType(ViewerType.PAGE)
        }
    }

    private fun onSearchActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        val page = data.getIntExtra(SearchActivity.EXTRA_SEARCH_RESULT_PAGE, 0)
        viewModel.setCurrentPage(page, true)
    }
}