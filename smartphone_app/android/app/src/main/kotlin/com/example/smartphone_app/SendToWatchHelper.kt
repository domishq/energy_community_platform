package com.example.energy

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object SendToWatchHelper {
    private const val TAG = "SendToWatchHelper"

    fun sendNetEnergy(context: Context, value: Int, measuredAt: Long) {
        val dataClient = Wearable.getDataClient(context)
        val request = PutDataMapRequest.create("/net_energy").apply {
            dataMap.putInt("netEnergy", value)
            dataMap.putLong("measuredAt", measuredAt)
            dataMap.putLong("time", System.currentTimeMillis()) // ensure uniqueness
        }.asPutDataRequest().setUrgent()

        // Use a background coroutine since Service has no lifecycleScope
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = dataClient.putDataItem(request).await()
                Log.d(TAG, "Sent to watch(value=$value, measuredAt=$measuredAt): $result")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send to watch, node may be offline", e)
            }
        }
    }
}
