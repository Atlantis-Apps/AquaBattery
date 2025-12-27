package com.atlantis.aquabattery.battery

data class BatteryInfo(
    val percent: Int,
    val isCharging: Boolean,
    val plugType: String,
    val health: String,
    val temperatureC: Float,
    val voltageMv: Int,
    val level: Int,
    val scale: Int
)