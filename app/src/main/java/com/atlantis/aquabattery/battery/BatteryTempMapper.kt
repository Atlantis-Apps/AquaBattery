package com.atlantis.aquabattery.battery

object BatteryTempMapper {

    fun status(tempC: Float): String {
        return when {
            tempC >= 45f -> "Hot ⚠️"
            tempC >= 38f -> "Warm"
            else -> "Normal"
        }
    }
}