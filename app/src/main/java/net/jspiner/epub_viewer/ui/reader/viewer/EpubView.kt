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
            BiFunction { pageInfo: PageInfo, _: ViewerType -> pageInfo }
        ).observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe { pageInfo ->
                val pageCount = viewModel.viewerTypeStrategy.getAllPageCount(pageInfo)
                adapter.setAllPageCount(pageCount)
                binding.verticalViewPager.currentItem = 0
            }

        viewModel.getViewerType()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe {
                viewModel.viewerTypeStrategy.changeViewPagerOrientation(binding.verticalViewPager)
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
        viewModel.viewerTypeStrategy.setCurrentPagerItem(
            binding.verticalViewPager,
            adapter,
            viewModel.getCurrentPageInfo(),
            currentPage
        )
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
                viewModel.viewerTypeStrategy.onWebViewScrolled(
                    binding.verticalViewPager,
                    viewModel,
                    scrollPosition
                )
            }.let { lastScrollDisposables.add(it) }
    }

    private fun onScrollToPrevSpine(fragment: WebContainerFragment, position: Int) {
        if (viewModel.getCurrentViewerType() == ViewerType.SCROLL) {
            fragment.scrollAfterLoading(
                viewModel.getCurrentPageInfo().spinePageList[position].height.toInt()
            )
        }
    }
}