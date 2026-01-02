package com.atlantis.aquabattery

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.max

class BatteryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<Int> = emptyList()
    private var smoothEnabled = true
    private var colorBlindMode = false
    private var drawProgress = 1f

    private val graphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.graph_baseline)
        alpha = 40
        strokeWidth = 2f
    }

    private val path = Path()

    fun setData(values: List<Int>) {
        data = values
        startFadeIn()
    }

    fun setSmoothEnabled(enabled: Boolean) {
        smoothEnabled = enabled
        invalidate()
    }

    fun setColorBlindMode(enabled: Boolean) {
        colorBlindMode = enabled
        invalidate()
    }

    private fun startFadeIn() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 450
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                drawProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()
        val maxVal = 100f

        drawGrid(canvas, w, h)

        graphPaint.color = if (colorBlindMode) {
            context.getColor(R.color.graph_line_cb)
        } else {
            context.getColor(R.color.graph_line)
        }

        path.reset()

        val stepX = w / max(1, data.size - 1)

        var prevX = 0f
        var prevY = h - (data[0] / maxVal) * h * drawProgress
        path.moveTo(prevX, prevY)

        for (i in 1 until data.size) {
            val x = i * stepX
            val y = h - (data[i] / maxVal) * h * drawProgress

            if (smoothEnabled) {
                val midX = (prevX + x) / 2f
                val midY = (prevY + y) / 2f
                path.quadTo(prevX, prevY, midX, midY)
            } else {
                path.lineTo(x, y)
            }

            prevX = x
            prevY = y
        }

        canvas.drawPath(path, graphPaint)
    }

    private fun drawGrid(canvas: Canvas, w: Float, h: Float) {
        val lines = 4
        val gap = h / lines

        for (i in 1 until lines) {
            val y = i * gap
            canvas.drawLine(0f, y, w, y, gridPaint)
        }
    }
}