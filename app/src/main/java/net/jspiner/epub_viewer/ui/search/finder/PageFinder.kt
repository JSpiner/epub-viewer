package net.jspiner.epub_viewer.ui.search.finder

import io.reactivex.Single
import net.jspiner.epubstream.dto.ItemRef

interface PageFinder {

    fun findPage(itemRef: ItemRef, index:Int): Single<Int>

}