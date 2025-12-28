package com.atlantis.aquabattery.battery

object TemperatureStatus {

    fun label(tempC: Float): String {
        return when {
            tempC >= 45f -> "Hot ⚠️"
            tempC >= 38f -> "Warm"
            else -> "Normal"
        }
    }
}