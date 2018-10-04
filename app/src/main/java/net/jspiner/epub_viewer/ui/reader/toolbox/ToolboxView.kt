package net.jspiner.epub_viewer.ui.reader.toolbox

import android.content.Context
import android.graphics.PointF
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
                setCurrentPageDisplay(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //no-op
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //no-op
            }
        })
    }

    @Deprecated("review bot test") // TODO : 코드리뷰봇 오류 감지 확인용 임시코드
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
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
                }
                else if (pointDistance(currentPoint, startTouchPoint) > CLICK_DISTANCE_LIMIT) {
                    toolboxClickable = false
                    touchSender(event)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!toolboxClickable) {
                    touchSender(event)
                }
                else {
                    performClick()
                }
            }
        }
        return true
    }

    private fun subscribe() {
        viewModel.getToolboxVisible()
            .subscribe { isVisible ->
                if (isVisible) showWindow() else hideWindow()
            }
        viewModel.getCurrentPage()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentPage -> setCurrentPageDisplay(currentPage) }
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
            .targetView(binding.root)
            .onStart { binding.root.visibility = View.VISIBLE }
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
            .targetView(binding.root)
            .onEnd { binding.root.visibility = View.INVISIBLE }
            .start()

        AnimationBuilder.builder()
            .translateAnimation(0f, 0f, 0f, 100f)
            .duration(ANIMATION_DURATION)
            .interpolator(AccelerateInterpolator())
            .targetView(binding.bottomToolbox)
            .start()
    }

    private fun setCurrentPageDisplay(currentPage: Int) {
        val pageInfo = viewModel.getPageInfo()
        binding.pageDisplay.text = "$currentPage / ${pageInfo.allPage}"
        binding.pageSeekbar.max = pageInfo.allPage
    }
}