package com.atlantis.aquabattery.battery

import android.os.BatteryManager

object BatteryHealthMapper {

    fun map(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD ->
                "Good (Normal wear)"

            BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                "Overheating ⚠️"

            BatteryManager.BATTERY_HEALTH_DEAD ->
                "Dead (Replace battery)"

            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                "Over voltage ⚠️"

            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE ->
                "Failure ⚠️"

            BatteryManager.BATTERY_HEALTH_COLD ->
                "Too cold ⚠️"

            else ->
                "Unknown"
        }
    }
}