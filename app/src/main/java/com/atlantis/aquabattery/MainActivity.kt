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
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.atlantis.aquabattery.battery.BatteryParser
import com.atlantis.aquabattery.battery.DrainEstimator
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
    private lateinit var switchSmoothGraph: Switch

    private lateinit var batteryRing: BatteryRingView
    private lateinit var batteryGraph: BatteryGraphView
    private lateinit var historyStore: BatteryHistoryStore

    private var lastUpdateTime = 0L

    private val prefs by lazy {
        getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    }

    private var colorBlindMode = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return

            val info = BatteryParser.parse(context, intent)

            tvPercent.text = "${info.percent}%"
            tvStatus.text = if (info.isCharging) "Charging" else "Discharging"
            tvSource.text = info.plugType
            tvHealth.text = "Health · ${explainHealth(info.health)}"

            val tempLabel = TemperatureStatus.label(info.temperatureC)
            tvTemp.text = "Temp · ${info.temperatureC} °C ($tempLabel)"

            tvVoltage.text = "Voltage · ${info.voltageMv} mV"
            tvLevelScale.text = "Level · ${info.level} / ${info.scale}"

            batteryRing.setBatteryState(info.percent, info.isCharging)

            historyStore.addPoint(info.percent)
            val history = historyStore.getPoints()
            batteryGraph.setData(history.map { it.second })

            tvDrain.text =
                "Drain · ${DrainEstimator.estimate(history, info.isCharging)}"

            updateLastUpdated()
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
        switchSmoothGraph = findViewById(R.id.switchSmoothGraph)

        batteryRing = findViewById(R.id.batteryRing)
        batteryGraph = findViewById(R.id.batteryGraph)

        historyStore = BatteryHistoryStore(this)

        val smoothEnabled = prefs.getBoolean("smooth_graph", true)
        colorBlindMode = prefs.getBoolean("color_blind", false)

        switchSmoothGraph.isChecked = smoothEnabled
        batteryGraph.setSmoothEnabled(smoothEnabled)

        batteryRing.setColorBlindMode(colorBlindMode)
        batteryGraph.setColorBlindMode(colorBlindMode)

        switchSmoothGraph.setOnCheckedChangeListener { _, enabled ->
            batteryGraph.setSmoothEnabled(enabled)
            prefs.edit().putBoolean("smooth_graph", enabled).apply()
        }
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
            "Dead" -> "Poor (Consider replacement)"
            "Cold" -> "Fair (Cold battery)"
            else -> raw
        }

    private fun updateLastUpdated() {
        val now = SystemClock.elapsedRealtime()
        val diff =
            if (lastUpdateTime == 0L) 0 else (now - lastUpdateTime) / 1000
        lastUpdateTime = now

        tvLastUpdated.text = when {
            diff <= 5 -> "Updated just now"
            diff < 60 -> "Updated ${diff}s ago"
            else -> "Updated ${max(1, diff / 60)} min ago"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.action_color_blind)?.isChecked = colorBlindMode
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {

            R.id.action_color_blind -> {
                colorBlindMode = !item.isChecked
                item.isChecked = colorBlindMode

                prefs.edit()
                    .putBoolean("color_blind", colorBlindMode)
                    .apply()

                batteryRing.setColorBlindMode(colorBlindMode)
                batteryGraph.setColorBlindMode(colorBlindMode)

                true
            }

            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
}