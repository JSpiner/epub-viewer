package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo

interface ViewerTypeStrategy {

    fun getAllPageCount(pageInfo: PageInfo): Int

}