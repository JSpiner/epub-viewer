package net.jspiner.epub_viewer.ui.reader.toolbox

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.PopupMenu
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import io.reactivex.android.schedulers.AndroidSchedulers
import net.jspiner.animation.AnimationBuilder
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ViewToolboxBinding
import net.jspiner.epub_viewer.ui.base.BaseView
import net.jspiner.epub_viewer.ui.etc.EtcActivity
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel

class ToolboxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseView<ViewToolboxBinding, ReaderViewModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.view_toolbox
    private lateinit var startTouchPoint: PointF
    private lateinit var startTouchEvent: MotionEvent
    private val CLICK_DISTANCE_LIMIT = 10
    private val ANIMATION_DURATION = 250L
    private var toolboxClickable = true
    var touchSender: (MotionEvent) -> Unit = { }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        fun setHeight(view: View, height: Int) {
            val params = view.layoutParams
            params.height = height
            view.layoutParams = params
        }

        subscribe()
        setHeight(binding.statusBarBackground, getActivity().getStatusBarHeight())
        setHeight(binding.navigationBarBackground, getActivity().getNavigationBarHeight())
        binding.pageSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //no-op
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //no-op
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                viewModel.setCurrentPage(seekBar.progress, true)
            }
        })
        binding.touchView.setOnTouchListener { _, event ->
            onTouchViewTouchEvent(event)
            return@setOnTouchListener true
        }
        binding.tocBtn.setOnClickListener { showTocPopupMenu() }
        binding.moreButton.setOnClickListener {
            val metaData = viewModel.extractedEpub.opf.metaData
            EtcActivity.startActivityForResult(
                getActivity(),
                metaData.title,
                metaData.creator?.creator ?: "",
                viewModel.toManifestItem(metaData.meta?.get("cover")!!)
            )
        }
    }

    private fun showTocPopupMenu() {
        val popupMenu = PopupMenu(context, binding.tocBtn)
        val navPoints = viewModel.extractedEpub.toc.navMap.navPoints

        for (navPoint in navPoints) {
            popupMenu.menu.add(navPoint.navLabel.text)
        }
        popupMenu.setOnMenuItemClickListener {menu ->
            for (navPoint in navPoints) {
                if (menu.title == navPoint.navLabel.text) {
                    viewModel.navigateToPoint(navPoint)
                    return@setOnMenuItemClickListener true
                }
            }
            throw RuntimeException("navPoint 를 찾을 수 없음 ${menu.title}")
        }

        popupMenu.show()
    }

    private fun pointDistance(point1: PointF, point2: PointF): Double {
        return Math.sqrt(
            Math.pow((point1.x - point2.x).toDouble(), 2.0) +
                Math.pow((point1.y - point2.y).toDouble(), 2.0)
        )
    }

    override fun performClick(): Boolean {
        val last = viewModel.getToolboxVisible().value!!
        viewModel.setToolboxVisible(!last)
        return super.performClick()
    }

    private val onTouchViewTouchEvent: (event: MotionEvent) -> Unit = { event ->
        val currentPoint = PointF(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTouchPoint = currentPoint
                startTouchEvent = event
                toolboxClickable = true
                touchSender(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!toolboxClickable) {
                    touchSender(event)
                } else if (pointDistance(currentPoint, startTouchPoint) > CLICK_DISTANCE_LIMIT) {
                    toolboxClickable = false
                    touchSender(event)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!toolboxClickable) {
                    touchSender(event)
                } else {
                    performClick()
                }
            }
        }
    }

    private fun subscribe() {
        viewModel.getToolboxVisible()
            .subscribe { isVisible ->
                if (isVisible) showWindow() else hideWindow()
            }
        viewModel.getCurrentPage()
            .map { it.first } // page
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { page ->
                println("page : $page")
                val pageInfo = viewModel.getPageInfo()

                binding.pageDisplay.text = "${page + 1} / ${pageInfo.allPage}"
                binding.pageSeekbar.max = pageInfo.allPage
                binding.pageSeekbar.progress = page

                var currentSpineIndex = -1
                for ((index, sumUntil) in pageInfo.pageCountSumList.withIndex()) {
                    currentSpineIndex = index
                    if (page < sumUntil) break
                }

                val currentId = viewModel.extractedEpub.opf.spine.itemrefs[currentSpineIndex].idRef
                for (navPoint in viewModel.extractedEpub.toc.navMap.navPoints) {
                    if (navPoint.id == currentId) {
                        binding.chapterName.text = navPoint.navLabel.text
                        break
                    }
                }
            }
    }

    private fun showWindow() {
        with(getActivity()) {
            showStatusBar()
            showNavigationBar()
        }

        AnimationBuilder.builder()
            .alphaAnimation(0f, 1f)
            .duration(ANIMATION_DURATION)
            .interpolator(DecelerateInterpolator())
            .targetView(binding.toolboxView)
            .onStart { binding.toolboxView.visibility = View.VISIBLE }
            .start()

        AnimationBuilder.builder()
            .translateAnimation(0f, 0f, 50f, 0f)
            .duration(ANIMATION_DURATION)
            .interpolator(DecelerateInterpolator())
            .targetView(binding.bottomToolbox)
            .start()
    }

    private fun hideWindow() {
        with(getActivity()) {
            hideStatusBar()
            hideNavigationBar()
        }

        AnimationBuilder.builder()
            .alphaAnimation(1f, 0f)
            .duration(ANIMATION_DURATION)
            .interpolator(AccelerateInterpolator())
            .targetView(binding.toolboxView)
            .onEnd { binding.toolboxView.visibility = View.INVISIBLE }
            .start()

        AnimationBuilder.builder()
            .translateAnimation(0f, 0f, 0f, 100f)
            .duration(ANIMATION_DURATION)
            .interpolator(AccelerateInterpolator())
            .targetView(binding.bottomToolbox)
            .start()
    }
}