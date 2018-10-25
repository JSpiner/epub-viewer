package net.jspiner.epub_viewer.ui.reader.strategy

import io.reactivex.subjects.CompletableSubject
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.epub_viewer.util.LifecycleTransformer

abstract class ViewerTypeStrategy(protected val viewModel: ReaderViewModel) {

    protected val pageInfo: PageInfo
        get() = viewModel.getCurrentPageInfo()
    private val lifecycleSubject: CompletableSubject  = CompletableSubject.create()

    abstract fun changeViewPagerOrientation(verticalViewPager: VerticalViewPager)

    abstract fun getAllPageCount(): Int

    abstract fun setCurrentPagerItem(
        pager: VerticalViewPager,
        adapter: EpubPagerAdapter,
        currentPage: Int
    )

    abstract fun onPagerItemSelected(pager: VerticalViewPager, adapter: EpubPagerAdapter, position: Int)

    protected fun <T> bindToLifecycle(): LifecycleTransformer<T> {
        return LifecycleTransformer(lifecycleSubject)
    }

    fun unSubscribe() {
        lifecycleSubject.onComplete()
    }
}