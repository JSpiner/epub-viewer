package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager

class ScrollTypeStrategy : ViewerTypeStrategy {

    override fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager) {
        verticalViewPager.verticalMode()
    }

    override fun getAllPageCount(pageInfo: PageInfo) = pageInfo.spinePageList.size

    override fun setCurrentPagerItem(
        pager: VerticalViewPager,
        adapter: EpubPagerAdapter,
        pageInfo: PageInfo,
        currentPage: Int
    ) {
        fun getScrollPosition(index: Int): Int {
            val deviceHeight = pager.context.resources.displayMetrics.heightPixels

            return if (index == 0) {
                0
            } else {
                (currentPage - pageInfo.pageCountSumList[index - 1]) * deviceHeight
            }
        }

        var spineIndex = -1
        var scrollPosition = 0
        for ((i, pageSum) in pageInfo.pageCountSumList.withIndex()) {
            if (currentPage + 1 <= pageSum) {
                spineIndex = i
                scrollPosition = getScrollPosition(i)
                break
            }
        }

        pager.currentItem = spineIndex
        adapter.getFragmentAt(spineIndex).scrollAfterLoading(scrollPosition)
    }
}