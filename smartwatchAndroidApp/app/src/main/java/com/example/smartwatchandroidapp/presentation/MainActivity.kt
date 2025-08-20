package com.example.smartwatchandroidapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.smartwatchandroidapp.R
import com.example.smartwatchandroidapp.presentation.theme.SmartwatchAndroidAppTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp(value = 4876)
        }
    }
}

@Composable
fun WearApp(value: Int) {
    SmartwatchAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            // Keep time at the very top
            TimeText(modifier = Modifier.align(Alignment.TopCenter).padding(12.dp))

            // Main indicator
            CircularValueIndicator(value = value, maxValue = 10000)
        }
    }
}

@Composable
fun CircularValueIndicator(value: Int, maxValue: Int) {
    val floatValue = (value / 1000f) // Scale int -> float
    val displayValue = String.format("%.2f", floatValue) // Max 2 decimals
    val progress = (abs(floatValue) / (maxValue / 1000f)).coerceIn(0f, 1f)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        // Draw circle around screen edge
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = 360 * progress
            val strokeWidth = 14.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2

            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)

            if (progress > 0f) {
                val brush = Brush.sweepGradient(
                    if (value >= 0) listOf(Color(0xFF4CAF50), Color(0xFF81C784)) // green shades
                    else listOf(Color(0xFFE53935), Color(0xFFFF7043))            // red shades
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

        // Center content: Number + kWh
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Main number
            Text(
                text = if (value == 0) "0.00" else displayValue,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Unit under number, slightly smaller and grayed out
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewCircularIndicator() {
    WearApp(value = -3500)
}
