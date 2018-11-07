package net.jspiner.viewer.ui.reader.strategy

import io.reactivex.subjects.CompletableSubject
import net.jspiner.viewer.dto.PageInfo
import net.jspiner.viewer.ui.reader.ReaderViewModel
import net.jspiner.viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.viewer.ui.reader.viewer.BiDirectionViewPager
import net.jspiner.viewer.util.LifecycleTransformer

abstract class ViewerTypeStrategy(protected val viewModel: ReaderViewModel) {

    protected val pageInfo: PageInfo
        get() = viewModel.getCurrentPageInfo()
    private val lifecycleSubject: CompletableSubject = CompletableSubject.create()

    abstract fun changeViewPagerOrientation(biDirectionViewPager: BiDirectionViewPager)

    abstract fun getAllPageCount(): Int

    abstract fun setCurrentPagerItem(
        pager: BiDirectionViewPager,
        adapter: EpubPagerAdapter,
        currentPage: Int
    )

    abstract fun onPagerItemSelected(pager: BiDirectionViewPager, adapter: EpubPagerAdapter, position: Int)

    protected fun <T> bindToLifecycle(): LifecycleTransformer<T> {
        return LifecycleTransformer(lifecycleSubject)
    }

    fun unSubscribe() {
        lifecycleSubject.onComplete()
    }
}