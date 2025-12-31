package com.atlantis.aquabattery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.atlantis.aquabattery.battery.BatteryParser
import com.atlantis.aquabattery.battery.TemperatureStatus
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var tvPercent: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSource: TextView
    private lateinit var tvHealth: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvLevelScale: TextView
    private lateinit var tvDrain: TextView
    private lateinit var tvLastUpdated: TextView
    private lateinit var ivCharging: ImageView

    private lateinit var batteryRing: BatteryRingView
    private lateinit var batteryGraph: BatteryGraphView
    private lateinit var historyStore: BatteryHistoryStore

    private var lastUpdateTime = 0L

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return

            val info = BatteryParser.parse(context, intent)

            // ===== BASIC =====
            tvPercent.text = "${info.percent}%"
            tvStatus.text = if (info.isCharging) "Charging" else "Discharging"
            tvSource.text = info.plugType
            tvHealth.text = "Health · ${explainHealth(info.health)}"
            tvVoltage.text = "Voltage · ${info.voltageMv} mV"
            tvLevelScale.text = "Level · ${info.level} / ${info.scale}"

            // ===== TEMPERATURE =====
            val tempLabel = TemperatureStatus.label(info.temperatureC)
            tvTemp.text = "Temp · ${info.temperatureC} °C ($tempLabel)"
            tvTemp.setTextColor(
                when (tempLabel) {
                    "Hot ⚠️" -> 0xFFFFCDD2.toInt()
                    "Warm" -> 0xFFFFF3E0.toInt()
                    else -> 0xFFB0BEC5.toInt()
                }
            )

            // ===== DRAIN (STATE ONLY) =====
            tvDrain.text = when {
                info.isCharging -> "Drain · Charging"
                else -> "Drain · Stable"
            }

            // ===== RING =====
            batteryRing.setBatteryState(info.percent, info.isCharging)

            // ===== HISTORY + GRAPH =====
            historyStore.addPoint(info.percent)
            val history = historyStore.getPoints()
            batteryGraph.setData(history.map { it.second })

            // ===== LAST UPDATED =====
            updateLastUpdated()

            // ===== CHARGING ICON =====
            setChargingAnimation(info.isCharging)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        tvPercent = findViewById(R.id.tvPercent)
        tvStatus = findViewById(R.id.tvStatus)
        tvSource = findViewById(R.id.tvSource)
        tvHealth = findViewById(R.id.tvHealth)
        tvTemp = findViewById(R.id.tvTemp)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvLevelScale = findViewById(R.id.tvLevelScale)
        tvDrain = findViewById(R.id.tvDrain)
        tvLastUpdated = findViewById(R.id.tvLastUpdated)
        ivCharging = findViewById(R.id.ivCharging)

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
            ivCharging.visibility = View.VISIBLE
            ivCharging.animate().cancel()
            ivCharging.alpha = 1f
            ivCharging.animate()
                .alpha(0.3f)
                .setDuration(900)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { setChargingAnimation(true) }
                .start()
        } else {
            ivCharging.animate().cancel()
            ivCharging.visibility = View.GONE
        }
    }

    private fun explainHealth(raw: String): String =
        when (raw) {
            "Good" -> "Good (Normal wear)"
            "Overheating" -> "Poor (Overheating)"
            "Dead" -> "Poor (Replace battery)"
            "Cold" -> "Fair (Cold battery)"
            else -> raw
        }

    private fun updateLastUpdated() {
        val now = SystemClock.elapsedRealtime()
        val diff = if (lastUpdateTime == 0L) 0 else (now - lastUpdateTime) / 1000
        lastUpdateTime = now

        tvLastUpdated.text = when {
            diff <= 5 -> "Updated just now"
            diff < 60 -> "Updated ${diff}s ago"
            else -> "Updated ${max(1, diff / 60)} min ago"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}