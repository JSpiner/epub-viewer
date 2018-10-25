package net.jspiner.epub_viewer.ui.search.finder

import android.content.Context
import io.reactivex.Single
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

abstract class PageFinder(val context: Context, val epub: Epub, val pageInfo: PageInfo) {

    abstract fun findPage(itemRef: ItemRef, index: Int): Single<Int>

    protected fun readFile(file: File): String {
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

    protected fun toManifestItem(itemRef: ItemRef): File {
        val manifestItemList = epub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return epub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : ${itemRef.idRef}")
    }
}