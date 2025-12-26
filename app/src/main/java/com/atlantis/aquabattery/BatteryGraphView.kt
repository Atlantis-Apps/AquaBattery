package com.atlantis.aquabattery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BatteryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1976D2.toInt()
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33000000
        strokeWidth = 2f
    }

    private var points: List<Pair<Long, Int>> = emptyList()

    fun setData(data: List<Pair<Long, Int>>) {
        points = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()
        val minT = points.first().first
        val maxT = points.last().first
        val rangeT = (maxT - minT).coerceAtLeast(1)

        // X axis
        canvas.drawLine(0f, h, w, h, axisPaint)

        var lastX = 0f
        var lastY = h

        points.forEachIndexed { i, (t, p) ->
            val x = (t - minT).toFloat() / rangeT * w
            val y = h - (p / 100f * h)

            if (i > 0) {
                canvas.drawLine(lastX, lastY, x, y, linePaint)
            }

            lastX = x
            lastY = y
        }
    }
}