package net.jspiner.epub_viewer.ui.reader.viewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewEpubViewerBinding
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

    init {
        subscribe()
        initPager()
    }

    fun sendTouchEvent(ev: MotionEvent) {
        super.dispatchTouchEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?) =  true

    private fun subscribe() {
        viewModel.getCurrentSpineItem()
            .map { viewModel.toManifestItem(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setSpineFile(it) }

        viewModel.getCurrentPage()
            .filter { it.second } // needUpdate
            .map { it.first } // page
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setCurrentPage(it) }
    }

    private fun setSpineFile(file: File) {
        val currentPosition = binding.verticalViewPager.currentItem
        adapter.getFragmentAt(currentPosition).loadFile(file)
    }

    private fun setCurrentPage(currentPage: Int) {
        if (measureCurrentPage() == currentPage) return

        fun getScrollPosition(index: Int): Int {
            val deviceHeight = context.resources.displayMetrics.heightPixels

            return if (index == 0) {
                0
            } else {
                (currentPage - viewModel.getPageInfo().pageCountSumList[index - 1]) * deviceHeight
            }
        }

        var spineIndex = -1
        var scrollPosition = 0
        for ((i, pageSum) in viewModel.getPageInfo().pageCountSumList.withIndex()) {
            if (currentPage + 1 <= pageSum) {
                spineIndex = i
                scrollPosition = getScrollPosition(i)
                break
            }
        }

        binding.verticalViewPager.currentItem = spineIndex
        adapter.getFragmentAt(spineIndex).scrollAfterLoading(scrollPosition)
    }

    private fun initPager() {
        binding.verticalViewPager.adapter = adapter
        binding.verticalViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                // no-op
            }

            override fun onPageSelected(position: Int) {
                viewModel.navigateToSpine(position)

                lastScrollDisposables.clear()

                val currentFragment = adapter.getFragmentAt(position)

                currentFragment
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

                currentFragment
                    .getScrollPosition()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { scrollPosition ->
                        val measuredPage = measureCurrentPage(scrollPosition)
                        viewModel.setCurrentPage(measuredPage, false)
                    }.let { lastScrollDisposables.add(it) }
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                // no-op
            }
        })
    }

    private fun measureCurrentPage(): Int {
        val currentFragment = adapter.getFragmentAt(binding.verticalViewPager.currentItem)
        return measureCurrentPage(currentFragment.getScrollPosition().value!!)
    }

    private fun measureCurrentPage(scrollPosition:Int): Int {
        val spinePosition = binding.verticalViewPager.currentItem
        val pageInfo = viewModel.getPageInfo()
        val deviceHeight = context.resources.displayMetrics.heightPixels

        val sumUntilPreview = if (spinePosition == 0) 0 else pageInfo.pageCountSumList[spinePosition - 1]

        return sumUntilPreview + (scrollPosition / deviceHeight)
    }
}