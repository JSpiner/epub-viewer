package net.jspiner.epub_viewer.ui.reader.viewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewEpubViewerBinding
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.dto.ViewerType
import net.jspiner.epub_viewer.ui.base.BaseView
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import java.io.File

class EpubView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewEpubViewerBinding, ReaderViewModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_epub_viewer
    private val adapter: EpubPagerAdapter by lazy {
        EpubPagerAdapter((getContext() as AppCompatActivity).supportFragmentManager)
    }
    private val lastScrollDisposables by lazy { CompositeDisposable() }
    private var lastSpineIndex = -1

    init {
        subscribe()
        initPager()
    }

    fun sendTouchEvent(ev: MotionEvent) {
        super.dispatchTouchEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?) = true

    private fun subscribe() {
        viewModel.getCurrentSpineItem()
            .map { viewModel.toManifestItem(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe { setSpineFile(it) }

        viewModel.getRawData()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe { setRawData(it.first.toURI().toURL().toString(), it.second) }

        viewModel.getCurrentPage()
            .filter { it.second } // needUpdate
            .map { it.first } // page
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe { setCurrentPage(it) }

        Observable.zip(
            viewModel.getPageInfo(),
            viewModel.getViewerType(),
            BiFunction { _: PageInfo, t2: ViewerType -> t2 }
        ).observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe { viewerType ->
                val pageInfo = viewModel.getCurrentPageInfo()
                when (viewerType) {
                    ViewerType.SCROLL -> adapter.setAllPageCount(pageInfo.spinePageList.size)
                    ViewerType.PAGE -> adapter.setAllPageCount(pageInfo.allPage)
                }
                binding.verticalViewPager.currentItem = 0
            }

        viewModel.getViewerType()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe {
                when (it!!) {
                    ViewerType.SCROLL -> binding.verticalViewPager.verticalMode()
                    ViewerType.PAGE -> binding.verticalViewPager.horizontalMode()
                }
            }
    }

    private fun setSpineFile(file: File) {
        val currentPosition = binding.verticalViewPager.currentItem
        adapter.getFragmentAt(currentPosition).loadFile(file)
    }

    private fun setRawData(baseUrl: String, string: String) {
        val currentPosition = binding.verticalViewPager.currentItem
        adapter.getFragmentAt(currentPosition).loadData(baseUrl, string)
    }

    private fun setCurrentPage(currentPage: Int) {
        when (viewModel.getCurrentViewerType()!!) {
            ViewerType.SCROLL -> setCurrentPageInScrollMode(currentPage)
            ViewerType.PAGE -> setCurrentPageInPageMode(currentPage)
        }
    }

    private fun setCurrentPageInScrollMode(currentPage: Int) {
        if (measureCurrentPage() == currentPage) return

        fun getScrollPosition(index: Int): Int {
            val deviceHeight = context.resources.displayMetrics.heightPixels

            return if (index == 0) {
                0
            } else {
                (currentPage - viewModel.getCurrentPageInfo().pageCountSumList[index - 1]) * deviceHeight
            }
        }

        var spineIndex = -1
        var scrollPosition = 0
        for ((i, pageSum) in viewModel.getCurrentPageInfo().pageCountSumList.withIndex()) {
            if (currentPage + 1 <= pageSum) {
                spineIndex = i
                scrollPosition = getScrollPosition(i)
                break
            }
        }

        binding.verticalViewPager.currentItem = spineIndex
        adapter.getFragmentAt(spineIndex).scrollAfterLoading(scrollPosition)
    }

    private fun setCurrentPageInPageMode(currentPage: Int) {
        binding.verticalViewPager.currentItem = currentPage
    }

    private fun initPager() {
        binding.verticalViewPager.adapter = adapter
        binding.verticalViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                // no-op
            }

            override fun onPageSelected(position: Int) {
                viewModel.navigateToIndex(position)

                when (viewModel.getCurrentViewerType()) {
                    ViewerType.SCROLL -> onPageSelectedInScrollMode(position)
                    ViewerType.PAGE -> onPageSelectedInPageMode(position)
                }
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                // no-op
            }
        })
    }

    private fun onPageSelectedInScrollMode(position: Int) {
        val currentFragment = adapter.getFragmentAt(position)
        subscribeScroll(currentFragment)

        if (lastSpineIndex == position + 1) onScrollToPrevSpine(currentFragment, position)
        lastSpineIndex = position
    }

    private fun onPageSelectedInPageMode(position: Int) {
        viewModel.setCurrentPage(position, false)
    }

    private fun subscribeScroll(fragment: WebContainerFragment) {
        lastScrollDisposables.clear()

        fragment
            .getScrollState()
            .distinctUntilChanged()
            .subscribe { scrollStatus ->
                when (scrollStatus) {
                    ScrollStatus.REACHED_TOP -> binding.verticalViewPager.enableScroll()
                    ScrollStatus.REACHED_BOTTOM -> binding.verticalViewPager.enableScroll()
                    ScrollStatus.NO_SCROLL -> binding.verticalViewPager.enableScroll()
                    ScrollStatus.SCROLLING -> binding.verticalViewPager.disableScroll()
                }
            }.let { lastScrollDisposables.add(it) }

        fragment
            .getScrollPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { scrollPosition ->
                if (viewModel.getCurrentViewerType() == ViewerType.SCROLL) {
                    val measuredPage = measureCurrentPage(scrollPosition)
                    viewModel.setCurrentPage(measuredPage, false)
                }
            }.let { lastScrollDisposables.add(it) }
    }

    private fun onScrollToPrevSpine(fragment: WebContainerFragment, position: Int) {
        if (viewModel.getCurrentViewerType() == ViewerType.SCROLL) {
            fragment.scrollAfterLoading(
                viewModel.getCurrentPageInfo().spinePageList[position].height.toInt()
            )
        }
    }

    private fun measureCurrentPage(): Int {
        val currentFragment = adapter.getFragmentAt(binding.verticalViewPager.currentItem)
        return measureCurrentPage(currentFragment.getScrollPosition().value ?: 0)
    }

    private fun measureCurrentPage(scrollPosition: Int): Int {
        return if (viewModel.getCurrentViewerType() == ViewerType.SCROLL) {
            val spinePosition = binding.verticalViewPager.currentItem
            val pageInfo = viewModel.getCurrentPageInfo()
            val deviceHeight = context.resources.displayMetrics.heightPixels

            val sumUntilPreview = if (spinePosition == 0) 0 else pageInfo.pageCountSumList[spinePosition - 1]

            sumUntilPreview + (scrollPosition / deviceHeight)
        } else {
            binding.verticalViewPager.currentItem
        }
    }
}