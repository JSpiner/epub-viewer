package net.jspiner.epub_viewer.ui.base

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class EpubWebClient: WebViewClient() {

    var isPageFinished = false
    var scrollPositionAfterLoading = 0

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.url?.let {
            view?.loadUrl(it.toString())
        }
        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        isPageFinished = false
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        isPageFinished = true

        if (scrollPositionAfterLoading != 0) {
            view.scrollY = scrollPositionAfterLoading
            scrollPositionAfterLoading = 0
        }
    }
}