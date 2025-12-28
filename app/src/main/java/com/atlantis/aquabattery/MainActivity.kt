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
import com.atlantis.aquabattery.battery.DrainEstimator
import com.atlantis.aquabattery.battery.ChargeSpeedEstimator
import com.atlantis.aquabattery.battery.TemperatureStatus
import com.atlantis.aquabattery.battery.VoltageStability
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

            // ===== PERCENT =====
            tvPercent.text =
                if (info.percent >= 0) "${info.percent}%" else "—"

            // ===== STATUS (FEATURE 3) =====
            tvStatus.text =
                if (info.isCharging)
                    ChargeSpeedEstimator.label(
                        historyStore.getPoints(),
                        info.isCharging
                    )
                else
                    "Discharging"

            // ===== DETAILS =====
            tvSource.text = info.plugType
            tvHealth.text = "Health · ${explainHealth(info.health)}"

            // ===== TEMPERATURE STATUS (FEATURE 4) =====
            val tempLabel = TemperatureStatus.label(info.temperatureC)
            tvTemp.text = "Temp · ${info.temperatureC} °C ($tempLabel)"
            when (tempLabel) {
                "Hot ⚠️" ->
                    tvTemp.setTextColor(0xFFFFCDD2.toInt())
                "Warm" ->
                    tvTemp.setTextColor(0xFFFFF3E0.toInt())
                else ->
                    tvTemp.setTextColor(0xFFB0BEC5.toInt())
            }

            // ===== VOLTAGE STABILITY (FEATURE 5) =====
            val voltageLabel = VoltageStability.label(info.voltageMv)
            tvVoltage.text = "Voltage · ${info.voltageMv} mV ($voltageLabel)"
            when (voltageLabel) {
                "Fluctuating ⚠️" ->
                    tvVoltage.setTextColor(0xFFFFCDD2.toInt())
                else ->
                    tvVoltage.setTextColor(0xFFB0BEC5.toInt())
            }

            tvLevelScale.text = "Level · ${info.level} / ${info.scale}"

            // ===== LAST UPDATED (FEATURE 2) =====
            updateLastUpdated()

            // ===== PERCENT COLOR =====
            when {
                info.percent <= 15 ->
                    tvPercent.setTextColor(0xFFFFCDD2.toInt())
                info.percent <= 40 ->
                    tvPercent.setTextColor(0xFFFFF3E0.toInt())
                else ->
                    tvPercent.setTextColor(0xFFFFFFFF.toInt())
            }

            // ===== RING =====
            if (info.percent >= 0) {
                batteryRing.setBatteryState(info.percent, info.isCharging)
            }

            // ===== HISTORY + DRAIN (FEATURE 6 READY) =====
            if (info.percent >= 0) {
                historyStore.addPoint(info.percent)
                val history = historyStore.getPoints()

                batteryGraph.setData(history.map { it.second })

                tvDrain.text =
                    "Drain · ${DrainEstimator.estimate(history, info.isCharging)}"
            }

            // ===== CHARGING ICON =====
            setChargingAnimation(info.isCharging)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== TOOLBAR =====
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ===== VIEWS =====
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

    // ===== FEATURE 1: HEALTH EXPLANATION =====
    private fun explainHealth(raw: String): String {
        return when (raw) {
            "Good" -> "Good (Normal wear)"
            "Overheating" -> "Poor (Overheating)"
            "Dead" -> "Poor (Consider replacement)"
            "Cold" -> "Fair (Cold battery)"
            else -> raw
        }
    }

    // ===== FEATURE 2: LAST UPDATED =====
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

    // ===== MENU =====
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}