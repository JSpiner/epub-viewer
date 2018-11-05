package net.jspiner.epub_viewer.ui.reader.viewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BiDirectionViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    private var isSwipeEnabled = true
    private var isVerticalMode = true

    init {
        setPageTransformer(true, VerticalPageTransformer())
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    private inner class VerticalPageTransformer : ViewPager.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            if (!isVerticalMode) {
                return
            }
            when {
                position < -1 -> view.alpha = 0f
                position <= 1 -> {
                    view.alpha = 1f
                    view.translationX = view.width * -position
                    view.translationY = position * view.height
                }
                else -> view.alpha = 0f
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isSwipeEnabled) {
            return false
        }
        if (!isVerticalMode) {
            return super.onInterceptTouchEvent(ev)
        }

        val intercepted = super.onInterceptTouchEvent(swapXY(ev))
        swapXY(ev)
        return intercepted
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isSwipeEnabled) {
            return false
        }
        if (!isVerticalMode) {
            return super.onTouchEvent(ev)
        }

        return super.onTouchEvent(swapXY(ev))
    }

    private fun swapXY(ev: MotionEvent): MotionEvent {
        val width = width.toFloat()
        val height = height.toFloat()

        val newX = ev.y / height * width
        val newY = ev.x / width * height

        ev.setLocation(newX, newY)

        return ev
    }

    fun enableScroll() {
        isSwipeEnabled = true
    }

    fun disableScroll() {
        isSwipeEnabled = false
    }

    fun verticalMode() {
        isVerticalMode = true
    }

    fun horizontalMode() {
        isVerticalMode = false
    }
}