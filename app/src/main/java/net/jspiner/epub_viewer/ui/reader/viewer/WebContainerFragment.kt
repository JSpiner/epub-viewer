package net.jspiner.epub_viewer.ui.reader.viewer

import android.os.Bundle
import android.view.ViewTreeObserver
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.FragmentWebContainerBinding
import net.jspiner.epub_viewer.ui.base.BaseFragment
import net.jspiner.epub_viewer.ui.base.EpubWebClient
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import java.io.File

class WebContainerFragment: BaseFragment<FragmentWebContainerBinding, ReaderViewModel>() {

    private var scrollStatusSubject = BehaviorSubject.createDefault(ScrollStatus.REACHED_TOP)

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
            }
            webView.webViewClient = EpubWebClient()

            webView.setOnScrollChangeListener { _, _, _, _, _ -> updateScrollState() }
            webView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        webView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        updateScrollState()
                    }
                }
            )
        }
    }

    fun getScrollState():Observable<ScrollStatus> = scrollStatusSubject

    fun loadFile(file: File) {
        binding.webView.loadUrl(file.toURI().toURL().toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scrollStatusSubject.onComplete()
    }
}