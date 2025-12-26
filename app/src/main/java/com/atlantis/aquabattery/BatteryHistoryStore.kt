package com.atlantis.aquabattery

import android.content.Context

class BatteryHistoryStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("battery_history", Context.MODE_PRIVATE)

    fun addPoint(percent: Int) {
        val time = System.currentTimeMillis()
        prefs.edit().putInt(time.toString(), percent).apply()
        trimOld()
    }

    fun getPoints(): List<Pair<Long, Int>> {
        return prefs.all
            .mapNotNull {
                val t = it.key.toLongOrNull()
                val p = it.value as? Int
                if (t != null && p != null) t to p else null
            }
            .sortedBy { it.first }
    }

    private fun trimOld() {
        val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val editor = prefs.edit()
        prefs.all.keys.forEach {
            val time = it.toLongOrNull() ?: return@forEach
            if (time < cutoff) editor.remove(it)
        }
        editor.apply()
    }
}