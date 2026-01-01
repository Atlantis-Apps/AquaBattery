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

    // ===== LOW BATTERY STATE =====
    private var isLowBattery = false
    private var pulseAnimator: ValueAnimator? = null

    private val baseStroke = 18f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = baseStroke
        strokeCap = Paint.Cap.ROUND
        isDither = true
        color = context.getColor(R.color.ring_track)
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = baseStroke
        strokeCap = Paint.Cap.ROUND
        isDither = true
        color = context.getColor(R.color.battery_good)
    }

    private val rect = RectF()

    private var percent = 0
    private var isCharging = false
    private var animOffset = 0f

    // ===== CHARGING ROTATION =====
    private val chargingAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2800
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            animOffset = it.animatedValue as Float
            invalidate()
        }
    }

    // ===== LOW BATTERY PULSE =====
    private fun startLowBatteryPulse() {
        if (pulseAnimator != null) return

        pulseAnimator = ValueAnimator.ofFloat(baseStroke - 4f, baseStroke + 6f).apply {
            duration = 900
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                ringPaint.strokeWidth = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopLowBatteryPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        ringPaint.strokeWidth = baseStroke
        invalidate()
    }

    // ===== PUBLIC API =====
    fun setBatteryState(percent: Int, charging: Boolean) {
        this.percent = percent.coerceIn(0, 100)
        this.isCharging = charging

        val low = this.percent <= 15

        // ---- LOW BATTERY HANDLING ----
        if (low && !isLowBattery) {
            isLowBattery = true
            ringPaint.color = context.getColor(R.color.battery_critical)
            startLowBatteryPulse()
        } else if (!low && isLowBattery) {
            isLowBattery = false
            stopLowBatteryPulse()
        }

        // ---- NORMAL COLOR LOGIC ----
        if (!isLowBattery) {
            ringPaint.color = when {
                percent <= 40 -> context.getColor(R.color.ring_progress)
                else -> context.getColor(R.color.battery_good)
            }
        }

        // ---- CHARGING ROTATION ----
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
        val pad = ringPaint.strokeWidth / 2f + 2f
        rect.set(pad, pad, size - pad, size - pad)

        // Background ring
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Progress ring
        val sweep = 360f * (percent / 100f)
        val startAngle = if (isCharging) -90f + animOffset else -90f
        canvas.drawArc(rect, startAngle, sweep, false, ringPaint)
    }
}