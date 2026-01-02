package com.atlantis.aquabattery

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
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
    private var touchIndex: Int? = null

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.graph_baseline)
        alpha = 40
        strokeWidth = 2f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
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
                touchIndex =
                    ((event.x / width) * data.size)
                        .toInt()
                        .coerceIn(0, data.size - 1)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                touchIndex = null
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()
        val maxVal = 100f
        val stepX = w / max(1, data.size - 1)

        drawGrid(canvas, w, h)

        for (i in 1 until data.size) {
            val prev = data[i - 1]
            val curr = data[i]

            val charging = curr > prev

            linePaint.color = when {
                colorBlindMode ->
                    context.getColor(R.color.battery_good)

                charging ->
                    context.getColor(R.color.battery_good)

                else ->
                    context.getColor(R.color.colorTextSecondary)
            }

            val x1 = (i - 1) * stepX
            val y1 = h - (prev / maxVal) * h * drawProgress
            val x2 = i * stepX
            val y2 = h - (curr / maxVal) * h * drawProgress

            path.reset()

            if (smoothEnabled) {
                val midX = (x1 + x2) / 2f
                val midY = (y1 + y2) / 2f
                path.moveTo(x1, y1)
                path.quadTo(x1, y1, midX, midY)
                path.quadTo(midX, midY, x2, y2)
            } else {
                path.moveTo(x1, y1)
                path.lineTo(x2, y2)
            }

            canvas.drawPath(path, linePaint)
        }

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