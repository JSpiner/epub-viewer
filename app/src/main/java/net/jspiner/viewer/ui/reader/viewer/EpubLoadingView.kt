package net.jspiner.viewer.ui.reader.viewer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class EpubLoadingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val CHILD_PADDING_LEFT = 20

    private val paint = Paint()

    init {
        paint.color = Color.rgb(240, 240, 240)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0..measuredHeight - 200 step (200)) {
            drawRect(canvas, i, 30, 30)
            drawRect(canvas, i + 40, 75, 20)
            drawRect(canvas, i + 70, 85, 20)
            drawRect(canvas, i + 100, 80, 20)
        }
    }

    private fun drawRect(canvas: Canvas, positionY: Int, widthPercent: Int, height: Int) {
        canvas.drawRect(
            Rect(
                CHILD_PADDING_LEFT,
                positionY,
                ((measuredWidth * widthPercent) / 100f).toInt(),
                positionY + height
            ),
            paint
        )
    }
}