package net.jspiner.epub_viewer.paginator

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.Page
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class PagePaginator(private val context: Context, private val extractedEpub: Epub) : Paginator(context, extractedEpub) {

    private val CALCULATE_PAGE_JS: String by lazy {
        val filePath = "PagePaginator.js"
        val stream = context.assets.open(filePath)

        BufferedReader(InputStreamReader(stream)).use { br ->
            val sb = StringBuilder()
            var line = br.readLine()

            while (line != null) {
                sb.append(line)
                sb.append(System.lineSeparator())
                line = br.readLine()
            }
            br.close()
            sb.toString()
        }
    }

    override fun calculatePage(): Single<PageInfo> {
        val itemRefList = extractedEpub.getItemRefs().toList()
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
        val contentHeightSubject = SingleSubject.create<List<Long>>()

        return Single.create<WebView> { emitter -> emitter.onSuccess(WebView(context)) }
            .doOnSuccess { setUpWebView(it) }
            .doOnSuccess { addJavascriptCallback(it, contentHeightSubject) }
            .doOnSuccess { it.loadUrl(fileUrl) }
            .zipWith(contentHeightSubject, BiFunction { _: WebView, indexList: List<Long> -> indexList })
            .map { indexList -> Page(0, indexList.size, indexList) }
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
                webView.loadUrl("javascript: $CALCULATE_PAGE_JS ")
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                val tag = "TAG"
                Log.i(
                    tag,
                    consoleMessage.message() + "\n" + consoleMessage.messageLevel() + "\n" + consoleMessage.lineNumber()
                )

                return super.onConsoleMessage(consoleMessage)
            }
        }
        webView.layout(0, 0, deviceWidth, deviceHeight)
    }

    private fun addJavascriptCallback(webView: WebView, subject: SingleSubject<List<Long>>) {

        class AndroidBridge {
            @JavascriptInterface
            fun result(indexString: String) {
                val indexList = indexString.split(",").map { it.toLong() }
                subject.onSuccess(indexList)
            }
        }
        webView.addJavascriptInterface(AndroidBridge(), "AndroidFunction")
    }
}