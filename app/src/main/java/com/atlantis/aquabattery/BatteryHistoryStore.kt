package com.atlantis.aquabattery

import android.content.Context
import android.os.SystemClock

class BatteryHistoryStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("battery_history", Context.MODE_PRIVATE)

    /**
     * Stores (elapsedRealtimeMs -> percent)
     */
    fun addPoint(percent: Int) {
        val time = SystemClock.elapsedRealtime()
        prefs.edit()
            .putInt(time.toString(), percent)
            .apply()
        trimOld()
    }

    /**
     * Returns sorted list of (timeMs, percent)
     */
    fun getPoints(): List<Pair<Long, Int>> {
        return prefs.all
            .mapNotNull { entry ->
                val t = entry.key.toLongOrNull()
                val p = entry.value as? Int
                if (t != null && p != null) t to p else null
            }
            .sortedBy { it.first }
    }

    /**
     * Keep only last 24h of ELAPSED time
     */
    private fun trimOld() {
        val cutoff = SystemClock.elapsedRealtime() - (24 * 60 * 60 * 1000)
        val editor = prefs.edit()

        prefs.all.keys.forEach { key ->
            val time = key.toLongOrNull() ?: return@forEach
            if (time < cutoff) {
                editor.remove(key)
            }
        }

        editor.apply()
    }
}