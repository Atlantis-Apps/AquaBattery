package com.atlantis.aquabattery

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BatteryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private var data: List<Int> = emptyList()
    private var smoothEnabled = true

    fun setData(values: List<Int>) {
        data = values
        invalidate()
    }

    fun setSmoothEnabled(enabled: Boolean) {
        smoothEnabled = enabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.size < 2) return

        val maxVal = 100f
        val w = width.toFloat()
        val h = height.toFloat()

        path.reset()

        val stepX = w / max(1, data.size - 1)

        var prevX = 0f
        var prevY = h - (data[0] / maxVal) * h
        path.moveTo(prevX, prevY)

        for (i in 1 until data.size) {
            val x = i * stepX
            val y = h - (data[i] / maxVal) * h

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

        canvas.drawPath(path, paint)
    }
}