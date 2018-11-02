package net.jspiner.viewer.ui.reader.viewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import net.jspiner.viewer.R
import net.jspiner.viewer.databinding.ViewEpubViewerBinding
import net.jspiner.viewer.dto.LoadData
import net.jspiner.viewer.dto.PageInfo
import net.jspiner.viewer.dto.ViewerType
import net.jspiner.viewer.ui.base.BaseView
import net.jspiner.viewer.ui.reader.ReaderViewModel

class EpubView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewEpubViewerBinding, ReaderViewModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_epub_viewer
    private val adapter: EpubPagerAdapter by lazy {
        EpubPagerAdapter((getContext() as AppCompatActivity).supportFragmentManager)
    }

    init {
        subscribe()
        initPager()
    }

    fun sendTouchEvent(ev: MotionEvent) {
        super.dispatchTouchEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?) = true

    private fun subscribe() {
        viewModel.getLoadData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe { setLoadData(it) }

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
            .subscribe {
                val pageCount = viewModel.viewerTypeStrategy.getAllPageCount()
                adapter.setAllPageCount(pageCount)
                binding.verticalViewPager.currentItem = 0

                viewModel.onPagerItemSelected(
                    binding.verticalViewPager,
                    adapter,
                    0
                )
            }

        viewModel.getViewerType()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
            .subscribe {
                viewModel.viewerTypeStrategy.changeViewPagerOrientation(binding.verticalViewPager)
            }
    }

    private fun setLoadData(loadData: LoadData) {
        val currentPosition = binding.verticalViewPager.currentItem
        adapter.getFragmentAt(currentPosition).loadData(loadData)
    }

    private fun setCurrentPage(currentPage: Int) {
        viewModel.viewerTypeStrategy.setCurrentPagerItem(
            binding.verticalViewPager,
            adapter,
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
                viewModel.onPagerItemSelected(binding.verticalViewPager, adapter, position)
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                // no-op
            }
        })
    }
}