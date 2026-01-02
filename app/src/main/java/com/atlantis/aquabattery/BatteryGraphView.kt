package com.atlantis.aquabattery

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BatteryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<Int> = emptyList()
    private var smoothEnabled = true
    private var colorBlindMode = false
    private var drawProgress = 1f

    private var touchIndex: Int? = null

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

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        color = context.getColor(R.color.colorTextSecondary)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.colorTextPrimary)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (data.size < 2) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                val index = ((event.x / width) * data.size)
                    .toInt()
                    .coerceIn(0, data.size - 1)

                touchIndex = index
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                touchIndex = null
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()
        val maxVal = 100f

        drawGrid(canvas, w, h)

        graphPaint.color =
            if (colorBlindMode)
                context.getColor(R.color.battery_good)
            else
                context.getColor(R.color.aqua_blue)

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

        touchIndex?.let { index ->
            val x = index * stepX
            val value = data[index]
            val y = h - (value / maxVal) * h

            canvas.drawLine(x, 0f, x, h, indicatorPaint)
            canvas.drawText("$value%", x, max(32f, y - 16f), labelPaint)
        }
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