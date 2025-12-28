package com.atlantis.aquabattery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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
import com.atlantis.aquabattery.battery.ChargeSpeed

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
            if (intent == null || context == null) return

            val info = BatteryParser.parse(context, intent)

            // ===== PERCENT =====
            tvPercent.text =
                if (info.percent >= 0) "${info.percent}%" else "—"

            // ===== STATUS (CHARGE SPEED) =====
            tvStatus.text =
                if (info.isCharging)
                    ChargeSpeed.label(
                        info.currentMa,
                        info.voltageMv,
                        info.plugType,
                        info.percent
                    )
                else
                    "Discharging"

            // ===== DETAILS =====
            tvSource.text = info.plugType
            tvHealth.text = "Health · ${explainHealth(info.health)}"
            tvTemp.text = "Temp · ${info.temperatureC} °C"
            tvVoltage.text = "Voltage · ${info.voltageMv} mV"
            tvLevelScale.text = "Level · ${info.level} / ${info.scale}"

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

            // ===== HISTORY + GRAPH =====
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
        ivCharging = findViewById(R.id.ivCharging)

        // Percent shadow for readability
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
    private fun explainHealth(rawHealth: String): String {
        return when (rawHealth) {
            "Good" -> "Good (Normal wear)"
            "Overheating" -> "Fair (Thermal stress)"
            "Cold" -> "Fair (Cold conditions)"
            "Over voltage" -> "Fair (Voltage irregularity)"
            "Dead" -> "Poor (Consider replacement)"
            else -> "Unknown"
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