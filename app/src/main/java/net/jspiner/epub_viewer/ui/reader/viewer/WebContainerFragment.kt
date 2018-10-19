package net.jspiner.epub_viewer.ui.reader.viewer

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.FragmentWebContainerBinding
import net.jspiner.epub_viewer.dto.LoadData
import net.jspiner.epub_viewer.dto.LoadType
import net.jspiner.epub_viewer.ui.base.BaseFragment
import net.jspiner.epub_viewer.ui.base.EpubWebClient
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import java.io.File

class WebContainerFragment : BaseFragment<FragmentWebContainerBinding, ReaderViewModel>() {

    private val CONTENT_CLEAR_URL = "about:blank"

    private var scrollStatusSubject = BehaviorSubject.createDefault(ScrollStatus.REACHED_TOP)
    private var scrollPositionSubject = BehaviorSubject.create<Int>()
    private val epubWebClient by lazy { EpubWebClient(pageFinishCallback) }

    companion object {
        fun newInstance(): WebContainerFragment {
            val fragment = WebContainerFragment()

            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_web_container

    override fun onStart() {
        super.onStart()
        initWebView()
    }

    private fun initWebView() {
        binding.webView.let { webView ->
            fun getContentHeight() = (webView.contentHeight * resources.displayMetrics.density.toDouble()).toInt()
            fun updateScrollState() {
                if (!epubWebClient.isPageFinished || epubWebClient.scrollPositionAfterLoading != 0) return

                val height = getContentHeight()
                val webViewHeight = webView.measuredHeight

                scrollStatusSubject.onNext(
                    when {
                        webView.scrollY + webViewHeight >= height -> ScrollStatus.REACHED_BOTTOM
                        webView.scrollY == 0 -> ScrollStatus.REACHED_TOP
                        webViewHeight >= height -> ScrollStatus.NO_SCROLL
                        else -> ScrollStatus.SCROLLING
                    }
                )
                scrollPositionSubject.onNext(webView.scrollY)
            }
            webView.webViewClient = epubWebClient

            webView.setOnScrollChangeListener { _, _, _, _, _ -> updateScrollState() }
            webView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        webView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        updateScrollState()
                    }
                }
            )
            webView.isHapticFeedbackEnabled = false
            webView.setOnLongClickListener { true }
        }
    }

    fun getScrollState(): Observable<ScrollStatus> = scrollStatusSubject

    fun getScrollPosition() = scrollPositionSubject

    fun loadData(loadData: LoadData) {
        when(loadData.loadType) {
            LoadType.RAW -> loadRawData(loadData.file, loadData.rawData!!)
            LoadType.FILE -> loadFile(loadData.file)
        }
    }

    private fun loadFile(file: File) {
        binding.webView.loadUrl(file.toURI().toURL().toString())
        binding.loadingView.visibility = VISIBLE
    }

    private fun loadRawData(file: File, rawString: String) {
        binding.webView.loadDataWithBaseURL(
            file.toURI().toURL().toString(),
            rawString,
            null,
            "utf-8",
            null
        )
        binding.loadingView.visibility = VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scrollStatusSubject.onComplete()
    }

    fun scrollAfterLoading(scrollPosition: Int) {
        if (epubWebClient.isPageFinished) {
            binding.webView.scrollY = scrollPosition
        } else {
            epubWebClient.scrollPositionAfterLoading = scrollPosition
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (!isVisibleToUser) {
            if (isBindingInitialized()) {
                binding.webView.loadUrl(CONTENT_CLEAR_URL)
                binding.loadingView.visibility = VISIBLE
            }
        }
    }

    private val pageFinishCallback: (url: String) -> Unit = { url ->
        if (url != CONTENT_CLEAR_URL) {
            binding.loadingView.visibility = GONE
        }
        if (epubWebClient.scrollPositionAfterLoading == 0 && binding.webView.scrollY == 0) scrollPositionSubject.onNext(
            0
        )
    }
}