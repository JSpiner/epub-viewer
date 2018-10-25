package net.jspiner.epub_viewer.ui.search.finder

import android.content.Context
import io.reactivex.Single
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class PagePageFinder(val context: Context, val epub: Epub, val pageInfo: PageInfo) : PageFinder {

    override fun findPage(itemRef: ItemRef, index: Int): Single<Int> {
        return Single.create { emitter ->
            val spineIndex = epub.opf.spine.itemrefs.indexOf(itemRef)
            val pageSum = if (spineIndex == 0) 0 else pageInfo.pageCountSumList[spineIndex - 1]

            val fullContent = readFile(toManifestItem(itemRef))
            val bodyStart = fullContent.indexOf("<body>") + "<body>".length
            val bodyEnd = fullContent.indexOf("</body")
            val body = fullContent.substring(bodyStart, bodyEnd)

            val splitList = body.split(" ")
            val splitLengthSumList = ArrayList<Int>()
            for (splitIndex in pageInfo.spinePageList[spineIndex].splitIndexList) {
                splitLengthSumList.add(
                    bodyStart
                        + splitList.subList(0, splitIndex.toInt())
                        .joinToString(" ").length
                )
            }

            for (i in 0..pageInfo.spinePageList[spineIndex].splitIndexList.size) {
                if (splitLengthSumList[i] + bodyStart >= index) {
                    emitter.onSuccess(pageSum + i)
                    return@create
                }
            }
            emitter.onSuccess(pageSum + pageInfo.spinePageList[spineIndex].splitIndexList.size)
        }
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