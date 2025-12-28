package com.atlantis.aquabattery.battery

object ChargingSpeedEstimator {

    fun estimate(
        percentHistory: List<Int>,
        isCharging: Boolean
    ): String {
        if (!isCharging || percentHistory.size < 2) {
            return "—"
        }

        val delta = percentHistory.last() - percentHistory.first()

        return when {
            delta >= 5 -> "Fast charging ⚡"
            delta >= 2 -> "Charging normally"
            delta >= 0 -> "Charging slowly"
            else -> "—"
        }
    }
}