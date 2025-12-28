package com.atlantis.aquabattery.battery

object ChargeSpeed {

    fun label(
        currentMa: Int,
        voltageMv: Int,
        plugType: String,
        percent: Int
    ): String {

        // Safety
        if (currentMa <= 0) return "Charging"

        val voltageV = voltageMv / 1000f

        // 🔥 Rapid / Super fast (PD / Turbo / Warp)
        if (currentMa >= 3000 || voltageV >= 9.5f) {
            return "Rapid charging ⚡⚡"
        }

        // ⚡ Fast (QC 3.0 / AFC)
        if (currentMa >= 1500 || voltageV >= 8.5f) {
            return "Fast charging ⚡"
        }

        // 🔌 AC normal charging (IMPORTANT FIX)
        if (plugType == "AC") {
            return "Charging"
        }

        // 🔋 High battery % tapering
        if (percent >= 80) {
            return "Charging (optimized)"
        }

        // 🐌 Only USB trickle should be slow
        return "Slow charging 🐌"
    }
}