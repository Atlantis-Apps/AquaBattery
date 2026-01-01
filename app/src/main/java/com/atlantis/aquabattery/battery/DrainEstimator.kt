package com.atlantis.aquabattery.battery

object DrainEstimator {

    fun estimate(
        history: List<Pair<Long, Int>>,
        charging: Boolean
    ): String {
        if (charging) return "Charging"
        if (history.size < 2) return "Calculating…"

        val latest = history.last()
        val cutoff = latest.first - (30 * 60 * 1000)
        val past = history.lastOrNull { it.first <= cutoff } ?: history.first()

        val diff = past.second - latest.second
        val timeMs = latest.first - past.first

        if (timeMs <= 0 || diff <= 0) return "Stable"

        val hours = timeMs / (1000f * 60f * 60f)
        val rate = diff / hours

        return when {
            rate >= 15f -> "Fast drain"
            rate >= 5f -> "Normal drain"
            else -> "Slow drain"
        }
    }
}