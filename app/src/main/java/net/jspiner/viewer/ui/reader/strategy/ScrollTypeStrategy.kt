package net.jspiner.viewer.ui.reader.strategy

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import net.jspiner.viewer.dto.LoadData
import net.jspiner.viewer.dto.LoadType
import net.jspiner.viewer.ui.reader.ReaderViewModel
import net.jspiner.viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.viewer.ui.reader.viewer.ScrollStatus
import net.jspiner.viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.viewer.ui.reader.viewer.WebContainerFragment

class ScrollTypeStrategy(viewModel: ReaderViewModel) : ViewerTypeStrategy(viewModel) {

    private val lastScrollDisposables by lazy { CompositeDisposable() }
    private var lastSpineIndex = -1

    override fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager) {
        verticalViewPager.verticalMode()
    }

    override fun getAllPageCount(): Int = pageInfo.spinePageList.size

    override fun setCurrentPagerItem(pager: VerticalViewPager, adapter: EpubPagerAdapter, currentPage: Int) {
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

    override fun onPagerItemSelected(pager: VerticalViewPager, adapter: EpubPagerAdapter, position: Int) {
        val currentFragment = adapter.getFragmentAt(position)
        subscribeScroll(currentFragment, pager)

        if (lastSpineIndex == position + 1) onScrollToPrevPagerItem(currentFragment, position)
        lastSpineIndex = position

        val itemRef = viewModel.extractedEpub.opf.spine.itemrefs[position]
        viewModel.setLoadData(
            LoadData(
                LoadType.FILE,
                viewModel.toManifestItem(itemRef)
            )
        )
    }

    private fun subscribeScroll(fragment: WebContainerFragment, pager: VerticalViewPager) {
        lastScrollDisposables.clear()

        fragment
            .getScrollState()
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .subscribe { scrollStatus ->
                when (scrollStatus) {
                    ScrollStatus.REACHED_TOP -> pager.enableScroll()
                    ScrollStatus.REACHED_BOTTOM -> pager.enableScroll()
                    ScrollStatus.NO_SCROLL -> pager.enableScroll()
                    ScrollStatus.SCROLLING -> pager.disableScroll()
                }
            }.let { lastScrollDisposables.add(it) }

        fragment
            .getScrollPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { scrollPosition ->
                onWebViewScrolled(
                    pager,
                    scrollPosition
                )
            }.let { lastScrollDisposables.add(it) }
    }

    private fun onWebViewScrolled(pager: VerticalViewPager, scrollPosition: Int) {
        val spinePosition = pager.currentItem
        val pageInfo = viewModel.getCurrentPageInfo()
        val deviceHeight = pager.context.resources.displayMetrics.heightPixels

        val sumUntilPreview = if (spinePosition == 0) 0 else pageInfo.pageCountSumList[spinePosition - 1]

        val measuredPage = sumUntilPreview + (scrollPosition / deviceHeight)
        viewModel.setCurrentPage(measuredPage, false)
    }

    private fun onScrollToPrevPagerItem(fragment: WebContainerFragment, position: Int) {
        fragment.scrollAfterLoading(
            pageInfo.spinePageList[position].height.toInt()
        )
    }
}