package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager

interface ViewerTypeStrategy {

    fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager)

    fun getAllPageCount(pageInfo: PageInfo): Int

}