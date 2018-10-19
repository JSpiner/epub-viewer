package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.epub_viewer.ui.reader.viewer.WebContainerFragment

abstract class ViewerTypeStrategy(protected val viewModel: ReaderViewModel) {

    abstract fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager)

    abstract fun getAllPageCount(pageInfo: PageInfo): Int

    abstract fun setCurrentPagerItem(
        pager: VerticalViewPager,
        adapter: EpubPagerAdapter,
        pageInfo: PageInfo,
        currentPage: Int
    )

    abstract fun onWebViewScrolled(pager: VerticalViewPager, viewModel: ReaderViewModel, scrollPosition: Int)

    abstract fun onScrollToPrevPagerItem(fragment: WebContainerFragment, currentPageInfo: PageInfo, position: Int)

    abstract fun onPagerItemSelected(viewModel: ReaderViewModel, pager: VerticalViewPager, adapter: EpubPagerAdapter, position: Int)
}