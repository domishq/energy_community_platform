package com.example.energy.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.energy.presentation.theme.SmartwatchAndroidAppTheme
import com.google.android.gms.wearable.Wearable
import java.time.Instant

// ---------------- EnergyRepository (cache helper) ----------------

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

// ---------------- MainActivity ----------------

class MainActivity : ComponentActivity() {

    private val netEnergy = mutableStateOf(0)

    private val netEnergyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getIntExtra(WearDataListenerService.EXTRA_NET_ENERGY, 0)?.let {
                netEnergy.value = it
                Log.d("MainActivity", "Updated netEnergy=$it from service")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // try to load cache first
        val repo = EnergyRepository(this)
        val cached = repo.getCachedValue()
        if (cached != null && repo.isCacheValid(cached.second)) {
            netEnergy.value = cached.first
        } else {
            // no cache → still display 0 but request from phone
            requestLatestFromPhone()
        }

        setContent {
            MaterialTheme {
                Scaffold {
                    EnergyScreen(value = netEnergy.value)
                }
            }
        }
    }

    private fun requestLatestFromPhone() {
        // Ask NodeClient for all connected nodes (usually just the paired phone)
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isEmpty()) {
                    Log.w("MainActivity", "No connected phone found")
                    return@addOnSuccessListener
                }

                val phoneNode = nodes.first()
                Wearable.getMessageClient(this)
                    .sendMessage(phoneNode.id, "/request_latest_energy", null)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Requested latest energy from ${phoneNode.displayName}")
                    }
                    .addOnFailureListener { error ->
                        Log.e("MainActivity", "Failed to request energy: ${error.message}", error)
                    }
            }
            .addOnFailureListener { error ->
                Log.e("MainActivity", "Error discovering phone node: ${error.message}", error)
            }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(netEnergyReceiver, IntentFilter(WearDataListenerService.ACTION_NET_ENERGY))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(netEnergyReceiver)
    }
}


@Composable
fun EnergyScreen(value: Int) {
    SmartwatchAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularValueIndicator(value = value, maxValue = 10000)
        }
    }
}

@Composable
fun CircularValueIndicator(value: Int, maxValue: Int) {
    val floatValue = value / 1000.0
    val displayValue = String.format("%.2f", floatValue)
    val progress = (kotlin.math.abs(floatValue) / (maxValue / 1000.0)).coerceIn(0.0, 1.0).toFloat()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = 360 * progress
            val strokeWidth = 14.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2
            val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
            val arcSize = Size(radius * 2, radius * 2)

            if (progress > 0f) {
                val brush = Brush.sweepGradient(
                    if (value >= 0) listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                    else listOf(Color(0xFFE53935), Color(0xFFFF7043))
                )
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = if (value >= 0) sweepAngle else -sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (value == 0) "0.00" else displayValue,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "kWh",
                color = Color(0xFFCCCCCC),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
