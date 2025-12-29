package com.atlantis.aquabattery.battery

import kotlin.math.abs

object TimeRemainingEstimator {

    /**
     * @param history List of Pair<timestampMillis, percent>
     * @param isCharging current charging state
     */
    fun estimate(
        history: List<Pair<Long, Int>>,
        isCharging: Boolean
    ): String {

        // Charging → no time-to-empty
        if (isCharging) return "Charging"

        // Need enough points to estimate
        if (history.size < 6) return "—"

        val first = history.first()
        val last = history.last()

        val percentDelta = first.second - last.second
        val timeDeltaMs = last.first - first.first

        // No drain or invalid data
        if (percentDelta <= 0 || timeDeltaMs <= 0) return "—"

        val hoursElapsed = timeDeltaMs / 3_600_000f
        val drainPerHour = percentDelta / hoursElapsed

        if (drainPerHour <= 0.1f) return "Stable"

        val hoursRemaining = last.second / drainPerHour

        val h = hoursRemaining.toInt()
        val m = ((hoursRemaining - h) * 60).toInt()

        return when {
            h <= 0 && m <= 0 -> "< 1h remaining"
            h <= 0 -> "~${m}m remaining"
            else -> "~${h}h ${m}m remaining"
        }
    }
}