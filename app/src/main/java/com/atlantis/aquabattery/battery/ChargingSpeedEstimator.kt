package com.atlantis.aquabattery.battery

object ChargeSpeedEstimator {

    fun label(
        history: List<Pair<Long, Int>>,
        charging: Boolean
    ): String {
        if (!charging) return "Discharging"
        if (history.size < 3) return "Charging"

        val rate = ratePerHour(history)

        return when {
            rate >= 20 -> "Fast charging ⚡"
            rate >= 8 -> "Charging normally"
            rate > 0 -> "Charging slowly"
            else -> "Charging"
        }
    }

    fun estimateToFull(
        history: List<Pair<Long, Int>>,
        currentPercent: Int
    ): String? {
        if (history.size < 3) return null

        val rate = ratePerHour(history)
        if (rate <= 0) return null

        val remaining = 100 - currentPercent
        val hours = remaining / rate

        val totalMinutes = (hours * 60).toInt()
        val h = totalMinutes / 60
        val m = totalMinutes % 60

        return when {
            h > 0 -> "${h}h ${m}m"
            else -> "${m}m"
        }
    }

    private fun ratePerHour(
        history: List<Pair<Long, Int>>
    ): Float {
        val (t1, p1) = history.first()
        val (t2, p2) = history.last()

        val deltaPercent = p2 - p1
        val deltaTimeMs = t2 - t1
        if (deltaTimeMs <= 0) return 0f

        val hours = deltaTimeMs / 3_600_000f
        return deltaPercent / hours
    }
}