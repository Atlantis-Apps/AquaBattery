package com.atlantis.aquabattery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.concurrent.TimeUnit

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
        val paddingLeft = 60f
        val paddingBottom = 48f
        val graphW = w - paddingLeft
        val graphH = h - paddingBottom

        val minT = points.first().first
        val maxT = points.last().first
        val rangeT = (maxT - minT).coerceAtLeast(1)

        // ===== AXES =====
        canvas.drawLine(paddingLeft, 0f, paddingLeft, graphH, axisPaint)
        canvas.drawLine(paddingLeft, graphH, w, graphH, axisPaint)

        // ===== Y LABELS =====
        drawYLabel(canvas, "100%", paddingLeft, 0f)
        drawYLabel(canvas, "50%", paddingLeft, graphH / 2)
        drawYLabel(canvas, "0%", paddingLeft, graphH)

        // ===== X LABELS =====
        val now = maxT
        drawXLabel(canvas, "1h", paddingLeft + graphW * 0.25f, graphH + 36f)
        drawXLabel(canvas, "30m", paddingLeft + graphW * 0.6f, graphH + 36f)
        drawXLabel(canvas, "Now", w - 8f, graphH + 36f, rightAlign = true)

        // ===== GRAPH LINE =====
        var lastX = 0f
        var lastY = 0f

        points.forEachIndexed { i, (t, p) ->
            val x =
                paddingLeft +
                ((t - minT).toFloat() / rangeT) * graphW

            val y =
                graphH - (p / 100f * graphH)

            if (i > 0) {
                canvas.drawLine(lastX, lastY, x, y, linePaint)
            }

            lastX = x
            lastY = y
        }
    }

    private fun drawYLabel(canvas: Canvas, text: String, x: Float, y: Float) {
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
        val drawX = if (rightAlign) x - textWidth else x - textWidth / 2
        canvas.drawText(text, drawX, y, labelPaint)
    }
}