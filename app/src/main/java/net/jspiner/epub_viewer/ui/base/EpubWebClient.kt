package net.jspiner.epub_viewer.ui.base

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class EpubWebClient: WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.url?.let {
            view?.loadUrl(it.toString())
        }
        return true
    }
}