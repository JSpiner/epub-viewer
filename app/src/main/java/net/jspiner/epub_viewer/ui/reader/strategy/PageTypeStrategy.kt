package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo

class PageTypeStrategy : ViewerTypeStrategy {

    override fun getAllPageCount(pageInfo: PageInfo) = pageInfo.allPage

}