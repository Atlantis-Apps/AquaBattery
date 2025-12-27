package com.atlantis.aquabattery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atlantis.aquabattery.battery.BatteryParser
import com.atlantis.aquabattery.battery.DrainEstimator

class MainActivity : AppCompatActivity() {

    private lateinit var tvPercent: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSource: TextView
    private lateinit var tvHealth: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvLevelScale: TextView
    private lateinit var tvDrain: TextView
    private lateinit var ivCharging: ImageView

    private lateinit var batteryRing: BatteryRingView
    private lateinit var batteryGraph: BatteryGraphView
    private lateinit var historyStore: BatteryHistoryStore

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val info = BatteryParser.parse(intent)

            // ===== TEXT =====
            tvPercent.text =
                if (info.percent >= 0) "${info.percent}%" else "—"

            tvStatus.text =
                if (info.isCharging) "Charging" else "Discharging"

            tvSource.text = info.plugType
            tvHealth.text = "Health · ${info.health}"
            tvTemp.text = "Temp · ${info.temperatureC} °C"
            tvVoltage.text = "Voltage · ${info.voltageMv} mV"
            tvLevelScale.text = "Level · ${info.level} / ${info.scale}"

            // ===== PERCENT COLOR =====
            when {
                info.percent <= 15 -> {
                    tvPercent.setTextColor(0xFFFFCDD2.toInt()) // soft red
                }
                info.percent <= 40 -> {
                    tvPercent.setTextColor(0xFFFFF3E0.toInt()) // amber
                }
                else -> {
                    tvPercent.setTextColor(0xFFFFFFFF.toInt()) // white
                }
            }

            // ===== RING =====
            if (info.percent >= 0) {
                batteryRing.setBatteryState(info.percent, info.isCharging)
            }

            // ===== HISTORY + GRAPH =====
            if (info.percent >= 0) {
                historyStore.addPoint(info.percent)
                val history = historyStore.getPoints()
                batteryGraph.setData(history)

                tvDrain.text =
                    "Drain · ${DrainEstimator.estimate(history, info.isCharging)}"
            }

            // ===== CHARGING ANIMATION =====
            setChargingAnimation(info.isCharging)
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
        tvDrain = findViewById(R.id.tvDrain)
        ivCharging = findViewById(R.id.ivCharging)

        // shadow so % always pops
        tvPercent.setShadowLayer(
            6f,
            0f,
            2f,
            0x55000000.toInt()
        )

        batteryRing = findViewById(R.id.batteryRing)
        batteryGraph = findViewById(R.id.batteryGraph)

        historyStore = BatteryHistoryStore(this)
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

    private fun setChargingAnimation(charging: Boolean) {
        if (charging) {
            if (ivCharging.visibility != View.VISIBLE) {
                ivCharging.visibility = View.VISIBLE
                ivCharging.alpha = 0f
            }

            ivCharging.animate().cancel()
            ivCharging.animate()
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    ivCharging.animate()
                        .alpha(0.3f)
                        .setDuration(800)
                        .withEndAction {
                            setChargingAnimation(true)
                        }
                        .start()
                }
                .start()
        } else {
            ivCharging.animate().cancel()
            ivCharging.visibility = View.GONE
        }
    }
}