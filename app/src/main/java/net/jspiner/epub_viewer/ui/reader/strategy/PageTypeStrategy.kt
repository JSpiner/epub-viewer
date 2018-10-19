package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.epub_viewer.ui.reader.viewer.WebContainerFragment

class PageTypeStrategy : ViewerTypeStrategy() {

    override fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager) {
        verticalViewPager.horizontalMode()
    }

    override fun getAllPageCount(pageInfo: PageInfo) = pageInfo.allPage

    override fun setCurrentPagerItem(
        pager: VerticalViewPager,
        adapter: EpubPagerAdapter,
        pageInfo: PageInfo,
        currentPage: Int
    ) {
        pager.currentItem = currentPage
    }

    override fun onWebViewScrolled(pager: VerticalViewPager, viewModel: ReaderViewModel, scrollPosition: Int) {
        // no-op
    }

    override fun onScrollToPrevPagerItem(fragment: WebContainerFragment, currentPageInfo: PageInfo, position: Int) {
        // no-op
    }

    override fun onPagerItemSelected(
        viewModel: ReaderViewModel,
        pager: VerticalViewPager,
        adapter: EpubPagerAdapter,
        position: Int
    ) {
        viewModel.setCurrentPage(position, false)
    }
}