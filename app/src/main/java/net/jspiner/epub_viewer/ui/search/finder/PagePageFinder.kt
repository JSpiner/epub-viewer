package net.jspiner.epub_viewer.ui.search.finder

import android.content.Context
import io.reactivex.Single
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef

class PagePageFinder(context: Context, epub: Epub, pageInfo: PageInfo) : PageFinder(context, epub, pageInfo) {

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
}