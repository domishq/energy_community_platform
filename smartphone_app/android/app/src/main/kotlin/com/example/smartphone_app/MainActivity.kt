package com.example.energy

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.energy.SendToWatchHelper

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.energy/wearos"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "sendNetEnergy") {
                val netEnergyStr = call.argument<String>("netEnergy") ?: "0"
                val measuredAt = call.argument<Long>("measuredAt") ?: System.currentTimeMillis()
                try {
                    val netEnergy = netEnergyStr.toDouble().toInt()
                    SendToWatchHelper.sendNetEnergy(this, netEnergy, measuredAt)
                    result.success(null)
                } catch (e: Exception) {
                    Log.e("FlutterBridge", "Failed to parse netEnergy: $netEnergyStr", e)
                    result.error("PARSE_ERROR", "Invalid netEnergy format: $netEnergyStr", null)
                }
            }
        }
    }
}