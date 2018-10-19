package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo

class ScrollTypeStrategy : ViewerTypeStrategy {

    override fun getAllPageCount(pageInfo: PageInfo) = pageInfo.spinePageList.size

}