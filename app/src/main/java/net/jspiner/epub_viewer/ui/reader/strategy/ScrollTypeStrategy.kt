package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.epub_viewer.ui.reader.viewer.WebContainerFragment

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

    override fun onWebViewScrolled(pager: VerticalViewPager, viewModel: ReaderViewModel, scrollPosition: Int) {
        val spinePosition = pager.currentItem
        val pageInfo = viewModel.getCurrentPageInfo()
        val deviceHeight = pager.context.resources.displayMetrics.heightPixels

        val sumUntilPreview = if (spinePosition == 0) 0 else pageInfo.pageCountSumList[spinePosition - 1]

        val measuredPage =sumUntilPreview + (scrollPosition / deviceHeight)
        viewModel.setCurrentPage(measuredPage, false)
    }

    override fun onScrollToPrevPagerItem(fragment: WebContainerFragment, currentPageInfo: PageInfo, position: Int) {
        fragment.scrollAfterLoading(
            currentPageInfo.spinePageList[position].height.toInt()
        )
    }
}