package com.example.energy.presentation

import android.util.Log
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.energy.SendToWatchHelper
import com.example.energy.EnergyRepository

class PhoneDataListenerService : WearableListenerService() {

    companion object {
        const val TAG = "PhoneDataListenerService"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/request_latest_energy") {
            Log.d(TAG, "Received request for latest energy from watch")

            val repo = EnergyRepository(this)
            val cached = repo.getCachedValue()

            if (cached != null && repo.isCacheValid(cached.second)) {
                val (value, measuredAt) = cached
                Log.d(TAG, "Using cached netEnergy=$value measuredAt=$measuredAt")
                SendToWatchHelper.sendNetEnergy(this, value, measuredAt)
            } else {
                Log.w(TAG, "No valid cache, fetching from API...")
                fetchAndSendFromApi(repo)
            }
        } else {
            super.onMessageReceived(messageEvent)
        }
    }

    private fun fetchAndSendFromApi(repo: EnergyRepository) {
        val communityId = "community1"
        val url = "http://10.0.2.2:8000/communities/$communityId"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    val genW = json.getInt("genW")
                    val conW = json.getInt("conW")
                    val netEnergy = genW - conW
                    val measuredAt = System.currentTimeMillis()

                    // save to repository
                    repo.saveValue(netEnergy, measuredAt)

                    SendToWatchHelper.sendNetEnergy(this, netEnergy, measuredAt)
                    Log.d(TAG, "Fetched fresh energy and sent to watch: $netEnergy")

                } else {
                    Log.w(TAG, "API returned $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch energy from API", e)
            }
        }.start()
    }
}
