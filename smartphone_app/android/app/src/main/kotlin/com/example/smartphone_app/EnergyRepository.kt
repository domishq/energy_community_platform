package com.example.energy

import android.content.Context
import java.time.Instant

class EnergyRepository(val context: Context) {
    private val prefs = context.getSharedPreferences("energy_cache", Context.MODE_PRIVATE)

    companion object {
        const val MAX_CACHE_MINUTES = 1L
    }

    fun getCachedValue(): Pair<Int, Long>? {
        val value = prefs.getInt("latest_energy", -1)
        val measuredAt = prefs.getLong("latest_measured_at", 0L)
        return if (value != -1) Pair(value, measuredAt) else null
    }

    fun saveValue(value: Int, measuredAt: Long) {
        prefs.edit()
            .putInt("latest_energy", value)
            .putLong("latest_measured_at", measuredAt)
            .apply()
    }

    fun isCacheValid(measuredAt: Long): Boolean {
        val now = Instant.now().toEpochMilli()
        val ageMillis = now - measuredAt
        val maxMillis = MAX_CACHE_MINUTES * 60 * 1000
        return ageMillis <= maxMillis
    }
}
