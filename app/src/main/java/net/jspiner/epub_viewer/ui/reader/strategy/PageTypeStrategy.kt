package net.jspiner.epub_viewer.ui.reader.strategy

import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager

class PageTypeStrategy(viewModel: ReaderViewModel) : ViewerTypeStrategy(viewModel) {

    override fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager) {
        verticalViewPager.horizontalMode()
    }

    override fun getAllPageCount(): Int = pageInfo.allPage

    override fun setCurrentPagerItem(pager: VerticalViewPager, adapter: EpubPagerAdapter, currentPage: Int) {
        pager.currentItem = currentPage
    }

    override fun onPagerItemSelected(pager: VerticalViewPager, adapter: EpubPagerAdapter, position: Int) {
        viewModel.setCurrentPage(position, false)
    }
}