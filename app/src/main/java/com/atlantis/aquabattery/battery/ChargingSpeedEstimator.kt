package com.atlantis.aquabattery.battery

import kotlin.math.abs
import kotlin.math.max

object ChargeSpeedEstimator {

    fun label(
        history: List<Pair<Long, Int>>,
        isCharging: Boolean
    ): String {
        if (!isCharging || history.size < 2) {
            return "Charging"
        }

        val first = history.first()
        val last = history.last()

        val deltaPercent = last.second - first.second
        val deltaTimeMs = last.first - first.first

        if (deltaPercent <= 0 || deltaTimeMs <= 0) {
            return "Charging"
        }

        val hours = deltaTimeMs / 3_600_000f
        val rate = deltaPercent / max(0.1f, hours)

        return when {
            rate >= 12f -> "Fast charging ⚡"
            rate >= 4f -> "Charging"
            else -> "Charging slowly"
        }
    }
}