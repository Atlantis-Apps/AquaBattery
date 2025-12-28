package com.atlantis.aquabattery.battery

object VoltageStability {

    private var lastVoltage: Int? = null

    fun label(currentVoltage: Int): String {
        val previous = lastVoltage
        lastVoltage = currentVoltage

        if (previous == null || currentVoltage <= 0) {
            return "Stable"
        }

        val delta = kotlin.math.abs(currentVoltage - previous)

        return if (delta > 120) {
            "Fluctuating ⚠️"
        } else {
            "Stable"
        }
    }
}