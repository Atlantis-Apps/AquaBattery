package com.atlantis.aquabattery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvPercent: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSource: TextView
    private lateinit var tvHealth: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvLevelScale: TextView
    private lateinit var tvPlugged: TextView

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent =
                if (level >= 0 && scale > 0) (level * 100) / scale else -1

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            val plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthText = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }

            val tempTenths =
                intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            val temperature =
                if (tempTenths != Int.MIN_VALUE) tempTenths / 10f else 0f

            val voltage =
                intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

            // UI updates
            tvPercent.text = if (percent >= 0) "$percent%" else "—"

            tvStatus.text =
                if (isCharging) "Charging" else "Discharging"

            tvSource.text =
                if (plug == 0) "Not plugged" else "Plugged"

            tvHealth.text = "Health: $healthText"
            tvTemp.text = "Temperature: ${temperature} °C"
            tvVoltage.text = "Voltage: ${voltage} mV"
            tvLevelScale.text = "Level: $level / Scale: $scale"
            tvPlugged.text = "Plugged code: $plug"

            // Battery % color (safe)
            when {
                percent <= 15 -> tvPercent.setTextColor(0xFFD32F2F.toInt())
                percent <= 40 -> tvPercent.setTextColor(0xFFF57C00.toInt())
                else -> tvPercent.setTextColor(0xFF1976D2.toInt())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvPercent = findViewById(R.id.tvPercent)
        tvStatus = findViewById(R.id.tvStatus)
        tvSource = findViewById(R.id.tvSource)
        tvHealth = findViewById(R.id.tvHealth)
        tvTemp = findViewById(R.id.tvTemp)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvLevelScale = findViewById(R.id.tvLevelScale)
        tvPlugged = findViewById(R.id.tvPlugged)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(batteryReceiver)
    }
}