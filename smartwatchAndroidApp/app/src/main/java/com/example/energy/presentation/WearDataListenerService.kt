package com.example.energy.presentation

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {

    companion object {
        const val ACTION_NET_ENERGY = "com.example.energy.NET_ENERGY_UPDATE"
        const val EXTRA_NET_ENERGY = "extra_net_energy"
    }

    private val TAG = "WearDataListenerService"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/net_energy") {

                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val value = dataMap.getInt("netEnergy", 0)
                Log.d(TAG, "Received netEnergy=$value")

                // Broadcast value to MainActivity
                val intent = Intent(ACTION_NET_ENERGY)
                intent.putExtra(EXTRA_NET_ENERGY, value)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }
}
