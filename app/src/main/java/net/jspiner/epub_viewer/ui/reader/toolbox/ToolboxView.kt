package net.jspiner.epub_viewer.ui.reader.toolbox

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
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
    private var toolboxClickable = true
    var touchSender: (MotionEvent) -> Unit = { }

    init {
        subscribe()
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
                with(getActivity()) {
                    if (isVisible) {
                        showStatusBar()
                        showNavigationBar()
                    } else {
                        hideStatusBar()
                        hideNavigationBar()
                    }
                }
            }
    }
}