package net.jspiner.epub_viewer.paginator

import android.content.Context
import io.reactivex.Single
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.Page
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epubstream.dto.ItemRef
import java.io.File

abstract class Paginator(private val context: Context, private val extractedEpub: Epub) {

    protected val WORKER_NUM = 8

    protected val deviceWidth: Int by lazy { context.resources.displayMetrics.widthPixels }
    protected val deviceHeight: Int by lazy { context.resources.displayMetrics.heightPixels }

    abstract fun calculatePage(): Single<PageInfo>

    protected fun toManifestItemPair(itemRef: ItemRef): Pair<ItemRef, File> {
        val manifestItemList = extractedEpub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return itemRef to extractedEpub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : $itemRef")
    }

    protected fun toIndexedList(itemRefList: List<ItemRef>, it: Map<String, Page>): ArrayList<Page> {
        val pageList = ArrayList<Page>()
        for (itemRef in itemRefList) {
            pageList.add(it[itemRef.idRef]!!)
        }
        return pageList
    }
}