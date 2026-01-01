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

    // ===== STATE =====
    private var percent = 0
    private var isCharging = false
    private var isLowBattery = false
    private var animOffset = 0f
    private var pulseAnimator: ValueAnimator? = null

    private val rect = RectF()

    // ===== BACKGROUND TRACK =====
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
        color = context.getColor(R.color.ring_track)
    }

    // ===== PROGRESS RING =====
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
        isDither = true
    }

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

        pulseAnimator = ValueAnimator.ofFloat(0.85f, 1f).apply {
            duration = 900
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                alpha = it.animatedValue as Float
            }
            start()
        }
    }

    private fun stopLowBatteryPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        alpha = 1f
    }

    // ===== PUBLIC API =====
    fun setBatteryState(percent: Int, charging: Boolean) {
        this.percent = percent.coerceIn(0, 100)
        this.isCharging = charging

        val low = this.percent <= 15

        // ----- LOW BATTERY -----
        if (low && !isLowBattery) {
            isLowBattery = true
            ringPaint.color = context.getColor(R.color.battery_critical)
            ringPaint.setShadowLayer(
                22f,
                0f,
                0f,
                context.getColor(R.color.battery_critical)
            )
            startLowBatteryPulse()
        }

        // ----- EXIT LOW BATTERY -----
        if (!low && isLowBattery) {
            isLowBattery = false
            stopLowBatteryPulse()
            ringPaint.clearShadowLayer()
        }

        // ----- NORMAL / CHARGING COLORS -----
        if (!isLowBattery) {
            when {
                charging -> {
                    ringPaint.color = context.getColor(R.color.battery_good)
                    ringPaint.setShadowLayer(
                        18f,
                        0f,
                        0f,
                        context.getColor(R.color.battery_good)
                    )
                }
                percent <= 40 -> {
                    ringPaint.color = context.getColor(R.color.ring_progress)
                    ringPaint.clearShadowLayer()
                }
                else -> {
                    ringPaint.color = context.getColor(R.color.battery_good)
                    ringPaint.clearShadowLayer()
                }
            }
        }

        // ----- CHARGING ROTATION -----
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

        // Background track
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Progress arc
        val sweep = 360f * (percent / 100f)
        val startAngle = if (isCharging) -90f + animOffset else -90f
        canvas.drawArc(rect, startAngle, sweep, false, ringPaint)
    }
}