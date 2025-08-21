package com.example.energy.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.*
import com.example.energy.presentation.theme.SmartwatchAndroidAppTheme
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column

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

        setContent {
            MaterialTheme {
                Scaffold {
                    EnergyScreen(value = netEnergy.value)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register BroadcastReceiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(netEnergyReceiver, IntentFilter(WearDataListenerService.ACTION_NET_ENERGY))
    }

    override fun onPause() {
        super.onPause()
        // Unregister BroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(netEnergyReceiver)
    }
}

@Composable
fun EnergyScreen(value: Int) {
    SmartwatchAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            TimeText(modifier = Modifier.align(Alignment.TopCenter).padding(12.dp))
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

