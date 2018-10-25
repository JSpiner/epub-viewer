package net.jspiner.epub_viewer.ui.search.finder

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef

class ScrollPageFinder(context: Context, epub: Epub, pageInfo: PageInfo) : PageFinder(context, epub, pageInfo) {

    private val deviceWidth: Int by lazy { context.resources.displayMetrics.widthPixels }
    private val deviceHeight: Int by lazy { context.resources.displayMetrics.heightPixels }

    override fun findPage(itemRef: ItemRef, index: Int): Single<Int> {
        val file = toManifestItem(itemRef)
        val splittedContent = readFile(file).substring(0, index)
        val contentHeightSubject = SingleSubject.create<Long>()

        return Single.create<WebView> { emitter -> emitter.onSuccess(WebView(context)) }
            .doOnSuccess { setUpWebView(it) }
            .doOnSuccess { addJavascriptCallback(it, contentHeightSubject) }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                it.loadDataWithBaseURL(
                    file.toURI().toURL().toString(),
                    splittedContent,
                    null,
                    "utf-8",
                    null
                )
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .zipWith(contentHeightSubject, BiFunction { _: WebView, height: Long -> height })
            .map { contentHeight ->
                val spineIndex = epub.opf.spine.itemrefs.indexOf(itemRef)
                val pageSum = if (spineIndex == 0) 0 else pageInfo.pageCountSumList[spineIndex - 1]
                val pageInSpine = (contentHeight / deviceHeight).toInt()

                return@map pageSum + pageInSpine
            }
            .subscribeOn(Schedulers.io())
    }

    private fun setUpWebView(webView: WebView) {
        webView.isVerticalScrollBarEnabled = true
        webView.settings.apply {
            builtInZoomControls = false
            javaScriptEnabled = true
            setSupportZoom(false)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                webView.loadUrl("javascript:AndroidFunction.resize(document.body.scrollHeight)")
            }
        }
        webView.layout(0, 0, deviceWidth, deviceHeight)
    }

    private fun addJavascriptCallback(webView: WebView, subject: SingleSubject<Long>) {
        webView.addJavascriptInterface(AndroidBridge(subject), "AndroidFunction")
    }

    inner class AndroidBridge(private val subject: SingleSubject<Long>) {
        @JavascriptInterface
        fun resize(height: Long) {
            val webViewHeight = height * context.resources.displayMetrics.density
            subject.onSuccess(webViewHeight.toLong())
        }
    }
}