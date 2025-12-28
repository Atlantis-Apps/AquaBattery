package com.atlantis.aquabattery

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class BatteryRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()

    private var percent = 0
    private var isCharging = false
    private var animOffset = 0f

    private val chargingAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            animOffset = it.animatedValue as Float
            invalidate()
        }
    }

    fun setBatteryState(percent: Int, charging: Boolean) {
        this.percent = percent.coerceIn(0, 100)
        this.isCharging = charging

        // 🔥 Progress ring colour (unchanged behaviour)
        ringPaint.color = when {
            percent <= 15 -> context.getColor(R.color.battery_critical)
            percent <= 40 -> context.getColor(R.color.ring_progress)
            else -> context.getColor(R.color.battery_good)
        }

        // 🔥 Background ring (outline) — GREY & MODE AWARE
        bgPaint.color = context.getColor(R.color.ring_track)

        if (charging && !chargingAnimator.isRunning) {
            chargingAnimator.start()
        } else if (!charging && chargingAnimator.isRunning) {
            chargingAnimator.cancel()
            animOffset = 0f
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val pad = 32f
        rect.set(pad, pad, size - pad, size - pad)

        // background ring (outline)
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // battery sweep
        val sweep = 360f * (percent / 100f)
        val startAngle = if (isCharging) -90f + animOffset else -90f

        canvas.drawArc(rect, startAngle, sweep, false, ringPaint)
    }
}