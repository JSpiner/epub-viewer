package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.epub_viewer.ui.reader.viewer.WebContainerFragment

interface ViewerTypeStrategy {

    fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager)

    fun getAllPageCount(pageInfo: PageInfo): Int

    fun setCurrentPagerItem(
        pager: VerticalViewPager,
        adapter: EpubPagerAdapter,
        pageInfo: PageInfo,
        currentPage: Int
    )

    fun onWebViewScrolled(pager: VerticalViewPager, viewModel: ReaderViewModel, scrollPosition: Int)

    fun onScrollToPrevPagerItem(fragment: WebContainerFragment, currentPageInfo: PageInfo, position: Int)
}