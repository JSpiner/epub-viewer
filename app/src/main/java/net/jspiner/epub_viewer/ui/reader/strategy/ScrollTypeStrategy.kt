package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager

class ScrollTypeStrategy : ViewerTypeStrategy {

    override fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager) {
        verticalViewPager.verticalMode()
    }

    override fun getAllPageCount(pageInfo: PageInfo) = pageInfo.spinePageList.size

}