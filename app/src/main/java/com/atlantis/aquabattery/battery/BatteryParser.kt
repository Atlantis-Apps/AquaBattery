package com.atlantis.aquabattery.battery

import android.content.Intent
import android.os.BatteryManager

object BatteryParser {

    fun parse(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent =
            if (level >= 0 && scale > 0) (level * 100) / scale else -1

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val plugType = when (
            intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        ) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not plugged"
        }

        val health = when (
            intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        ) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val tempTenths =
            intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val temp =
            if (tempTenths != Int.MIN_VALUE) tempTenths / 10f else 0f

        val voltage =
            intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        return BatteryInfo(
            percent,
            isCharging,
            plugType,
            health,
            temp,
            voltage,
            level,
            scale
        )
    }
}