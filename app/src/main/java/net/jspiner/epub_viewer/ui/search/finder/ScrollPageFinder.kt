package net.jspiner.epub_viewer.ui.search.finder

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.SingleSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class ScrollPageFinder(val context: Context, val epub: Epub, val pageInfo: PageInfo) : PageFinder {

    private val deviceWidth: Int by lazy { context.resources.displayMetrics.widthPixels }
    private val deviceHeight: Int by lazy { context.resources.displayMetrics.heightPixels }

    override fun findPage(itemRef: ItemRef, index: Int): Int {
        val file = toManifestItem(itemRef)
        val splittedContent = readFile(file).substring(0, index)
        val contentHeightSubject = SingleSubject.create<Long>()

        val contentHeight = Single.create<WebView> { emitter -> emitter.onSuccess(WebView(context)) }
            .doOnSuccess { setUpWebView(it) }
            .doOnSuccess { addJavascriptCallback(it, contentHeightSubject) }
            .doOnSuccess { it.loadDataWithBaseURL(file.toURI().toURL().toString(), splittedContent, null, "utf-8", null) }
            .zipWith(contentHeightSubject, BiFunction { _: WebView, height: Long -> height })
            .subscribeOn(AndroidSchedulers.mainThread())
            .blockingGet()

        val spineIndex = epub.opf.spine.itemrefs.indexOf(itemRef)
        val pageSum = if (spineIndex == 0) 0 else pageInfo.pageCountSumList[spineIndex - 1]
        val pageInSpine = (contentHeight / deviceHeight).toInt()

        return pageSum + pageInSpine
    }

    private fun readFile(file: File): String {
        return BufferedReader(FileReader(file)).use { br ->
            val sb = StringBuilder()
            var line = br.readLine()

            while (line != null) {
                sb.append(line)
                sb.append(System.lineSeparator())
                line = br.readLine()
            }
            sb.toString()
        }
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

    private fun toManifestItem(itemRef: ItemRef): File {
        val manifestItemList = epub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return epub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : ${itemRef.idRef}")
    }
}