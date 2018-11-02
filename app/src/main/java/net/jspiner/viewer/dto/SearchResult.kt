package net.jspiner.viewer.dto

import android.text.Spannable

data class SearchResult(
    val contentDisplay: Spannable,
    val page: Int
)