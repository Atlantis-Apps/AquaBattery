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
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33000000
        strokeWidth = 2f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF607D8B.toInt()
        textSize = 28f
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

        // ===== SAFE DRAWING PADDING =====
        val paddingLeft = 60f
        val paddingTop = 32f    
        val paddingBottom = 64f   

        val graphWidth = w - paddingLeft
        val graphHeight = h - paddingTop - paddingBottom

        if (graphHeight <= 0f) return

        // ===== TIME RANGE =====
        val minTime = points.first().first
        val maxTime = points.last().first
        val timeRange = (maxTime - minTime).coerceAtLeast(1L)

        val axisBottom = paddingTop + graphHeight

        // ===== AXES =====
        canvas.drawLine(
            paddingLeft,
            paddingTop,
            paddingLeft,
            axisBottom,
            axisPaint
        )

        canvas.drawLine(
            paddingLeft,
            axisBottom,
            w,
            axisBottom,
            axisPaint
        )

        // ===== Y-AXIS LABELS =====
        drawYLabel(canvas, "100%", paddingTop)
        drawYLabel(canvas, "50%", paddingTop + graphHeight / 2f)
        drawYLabel(canvas, "0%", axisBottom)

        // ===== X-AXIS LABELS =====
        drawXLabel(canvas, "1h", paddingLeft + graphWidth * 0.25f, axisBottom + 40f)
        drawXLabel(canvas, "30m", paddingLeft + graphWidth * 0.6f, axisBottom + 40f)
        drawXLabel(canvas, "Now", w - 8f, axisBottom + 40f, rightAlign = true)

        // ===== GRAPH LINE =====
        var lastX = 0f
        var lastY = 0f

        points.forEachIndexed { index, (time, percent) ->
            val x =
                paddingLeft +
                ((time - minTime).toFloat() / timeRange) * graphWidth

            val y =
                paddingTop + graphHeight -
                (percent.coerceIn(0, 100) / 100f * graphHeight)

            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, linePaint)
            }

            lastX = x
            lastY = y
        }
    }

    private fun drawYLabel(canvas: Canvas, text: String, y: Float) {
        canvas.drawText(text, 0f, y + 10f, labelPaint)
    }

    private fun drawXLabel(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        rightAlign: Boolean = false
    ) {
        val textWidth = labelPaint.measureText(text)
        val drawX =
            if (rightAlign) x - textWidth else x - textWidth / 2f

        canvas.drawText(text, drawX, y, labelPaint)
    }
}