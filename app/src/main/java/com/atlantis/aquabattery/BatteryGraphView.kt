package com.atlantis.aquabattery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class BatteryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.aqua_blue)
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isDither = true
    }

    private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.graph_baseline)
        strokeWidth = 1.5f
    }

    private val path = Path()
    private var data: List<Int> = emptyList()

    fun setData(points: List<Int>) {
        data = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()
        val pad = 20f

        val usableWidth = w - pad * 2
        val usableHeight = h - pad * 2

        // ===== BASELINE (100%) =====
        canvas.drawLine(
            pad,
            pad,
            w - pad,
            pad,
            baselinePaint
        )

        val stepX = usableWidth / (data.size - 1)

        path.reset()

        data.forEachIndexed { index, value ->
            val x = pad + stepX * index
            val clamped = value.coerceIn(0, 100)
            val y = pad + usableHeight * (1f - clamped / 100f)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, linePaint)
    }
}