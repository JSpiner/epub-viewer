package net.jspiner.epub_viewer.dto

import android.text.Spannable

data class SearchResult(
    val contentDisplay: Spannable,
    val page: Int
)