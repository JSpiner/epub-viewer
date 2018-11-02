package net.jspiner.viewer.paginator

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import net.jspiner.viewer.dto.Epub
import net.jspiner.viewer.dto.Page
import net.jspiner.viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef
import java.io.File

class ScrollPaginator(private val context: Context, private val extractedEpub: Epub) :
    Paginator(context, extractedEpub) {

    override fun calculatePage(): Single<PageInfo> {
        val itemRefList = extractedEpub.opf.spine.itemrefs.toList()
        val startTime = System.currentTimeMillis()

        return Observable.fromIterable(itemRefList)
            .toFlowable(BackpressureStrategy.BUFFER)
            .parallel(WORKER_NUM)
            .runOn(Schedulers.computation())
            .map { toManifestItemPair(it) }
            .map { measurePageInWebView(it) }
            .sequential()
            .toMap({ it.first.idRef }, { it.second })
            .map { toIndexedList(itemRefList, it) }
            .map { PageInfo.create(it) }
            .doOnSuccess { println("job done : ${System.currentTimeMillis() - startTime}") }
    }

    private fun measurePageInWebView(pair: Pair<ItemRef, File>): Pair<ItemRef, Page> {
        fun heightToPageNum(height: Long) = Math.ceil(height.toDouble() / deviceHeight.toDouble()).toInt()

        val itemRef = pair.first
        val fileUrl = pair.second.toURI().toURL().toString()
        val contentHeightSubject = SingleSubject.create<Long>()

        return Single.create<WebView> { emitter -> emitter.onSuccess(WebView(context)) }
            .doOnSuccess { setUpWebView(it) }
            .doOnSuccess { addJavascriptCallback(it, contentHeightSubject) }
            .doOnSuccess { it.loadUrl(fileUrl) }
            .zipWith(contentHeightSubject, BiFunction { _: WebView, height: Long -> height })
            .map { height -> Page(height, heightToPageNum(height)) }
            .map { page -> itemRef to page }
            .subscribeOn(AndroidSchedulers.mainThread())
            .blockingGet()
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
        class AndroidBridge {
            @JavascriptInterface
            fun resize(height: Long) {
                val webViewHeight = height * context.resources.displayMetrics.density
                subject.onSuccess(webViewHeight.toLong())
            }
        }
        webView.addJavascriptInterface(AndroidBridge(), "AndroidFunction")
    }
}