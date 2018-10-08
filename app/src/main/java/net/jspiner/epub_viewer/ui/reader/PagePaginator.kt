package net.jspiner.epub_viewer.ui.reader

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
import java.io.File

class PagePaginator(private val context: Context, private val extractedEpub: Epub) {

    private val CALCULATE_PAGE_JS = """
    var body = document.body.innerHTML;
    var deviceHeight = window.innerHeight;
    var words = body.split(' ');

    var pageText = "";
    for (var i = 0; i < words.length; i++) {
        pageText += ' ' + words[i];
        document.body.innerHTML = pageText;

        if (document.body.scrollHeight > deviceHeight) {
            AndroidFunction.result(i);
            pageText = "";
        }
    }
    if (pageText != "") {
        AndroidFunction.result(words.length - 1);
    }
    AndroidFunction.result(-1);
    """.trimIndent()

    private val WORKER_NUM = 8

    private val deviceWidth: Int by lazy { context.resources.displayMetrics.widthPixels }
    private val deviceHeight: Int by lazy { context.resources.displayMetrics.heightPixels }

    fun calculatePage(): Single<PageInfo> {
        val itemRefList = extractedEpub.opf.spine.itemrefs.toList()

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
    }

    private fun toManifestItemPair(itemRef: ItemRef): Pair<ItemRef, File> {
        val manifestItemList = extractedEpub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return itemRef to extractedEpub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : $itemRef")
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
            .map { indexList -> Page(0, indexList.size) }
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
                Log.i(tag, consoleMessage.message() + "\n" + consoleMessage.messageLevel() + "\n" + consoleMessage.lineNumber())

                return super.onConsoleMessage(consoleMessage)
            }
        }
        webView.layout(0, 0, deviceWidth, deviceHeight)
    }

    private fun addJavascriptCallback(webView: WebView, subject: SingleSubject<List<Long>>) {

        val indexList = ArrayList<Long>()

        class AndroidBridge {
            @JavascriptInterface
            fun result(spineIndex: Long) {
                if (spineIndex != -1L) {
                    subject.onSuccess(indexList)
                }
                else{
                    indexList.add(spineIndex)
                }
            }
        }
        webView.addJavascriptInterface(AndroidBridge(), "AndroidFunction")
    }

    private fun toIndexedList(itemRefList: List<ItemRef>, it: Map<String, Page>): ArrayList<Page> {
        val pageList = ArrayList<Page>()
        for (itemRef in itemRefList) {
            pageList.add(it[itemRef.idRef]!!)
        }
        return pageList
    }
}