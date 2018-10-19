package net.jspiner.epub_viewer.dto

import java.io.File

data class LoadData(
    val loadType: LoadType,
    val file: File,
    val rawData: String? = null
)

enum class LoadType {
    RAW,
    FILE
}