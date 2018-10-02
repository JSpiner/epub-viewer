package net.jspiner.epub_viewer.ui.reader.viewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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
    private var lastScrollDisposable: Disposable? = null

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
    }

    private fun setSpineFile(file: File) {
        val currentPosition = binding.verticalViewPager.currentItem
        adapter.getFragmentAt(currentPosition).loadFile(file)
    }

    private fun initPager() {
        binding.verticalViewPager.adapter = adapter
        binding.verticalViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                // no-op
            }

            override fun onPageSelected(position: Int) {
                viewModel.navigateToPoint(viewModel.extractedEpub.toc.navMap.navPoints[position])

                lastScrollDisposable?.dispose()
                adapter.getFragmentAt(position)
                    .getScrollState()
                    .distinctUntilChanged()
                    .subscribe { scrollStatus ->
                        when (scrollStatus) {
                            ScrollStatus.REACHED_TOP -> binding.verticalViewPager.enableScroll()
                            ScrollStatus.REACHED_BOTTOM -> binding.verticalViewPager.enableScroll()
                            ScrollStatus.NO_SCROLL -> binding.verticalViewPager.enableScroll()
                            ScrollStatus.SCROLLING -> binding.verticalViewPager.disableScroll()
                        }
                    }.let { lastScrollDisposable = it }
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                // no-op
            }
        })
    }
}