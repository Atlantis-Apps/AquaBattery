package com.atlantis.aquabattery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class BatteryRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        color = 0x33000000
        strokeCap = Paint.Cap.ROUND
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()
    private var percent = 0

    fun setBatteryPercent(value: Int) {
        percent = value.coerceIn(0, 100)
        ringPaint.color = when {
            percent <= 15 -> 0xFFD32F2F.toInt() // red
            percent <= 40 -> 0xFFF57C00.toInt() // orange
            else -> 0xFF1976D2.toInt()          // blue
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val pad = 32f
        rect.set(pad, pad, size - pad, size - pad)

        // background ring
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // battery ring
        val sweep = 360f * (percent / 100f)
        canvas.drawArc(rect, -90f, sweep, false, ringPaint)
    }
}