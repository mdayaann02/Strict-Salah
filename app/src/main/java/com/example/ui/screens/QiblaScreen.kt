package com.example.ui.screens

import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.qibla.QiblaCalculator
import com.example.data.qibla.QiblaSensorManager
import com.example.ui.viewmodel.MainUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    uiState: MainUiState,
    onRefreshLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile = uiState.profile

    // Sensor Manager
    val sensorManager = remember { QiblaSensorManager(context) }
    val sensorState by sensorManager.sensorState.collectAsState()

    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    // Qibla calculations
    val qiblaBearing = remember(profile.latitude, profile.longitude) {
        QiblaCalculator.calculateQiblaBearing(profile.latitude, profile.longitude)
    }
    val distanceKm = remember(profile.latitude, profile.longitude) {
        QiblaCalculator.calculateDistanceToKaabaKm(profile.latitude, profile.longitude)
    }
    val cardinal = remember(qiblaBearing) {
        QiblaCalculator.getCardinalDirection(qiblaBearing)
    }

    // Calculate heading difference to Qibla (-180..+180)
    var angleDifference = (qiblaBearing - sensorState.azimuth).toFloat()
    while (angleDifference < -180f) angleDifference += 360f
    while (angleDifference > 180f) angleDifference -= 360f

    val isFacingQibla = abs(angleDifference) <= 3.5f

    // Vibrator for haptic feedback when aligned
    var hasVibratedForCurrentAlignment by remember { mutableStateOf(false) }
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !hasVibratedForCurrentAlignment) {
            hasVibratedForCurrentAlignment = true
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(VibratorManager::class.java)
                    vibratorManager?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(80)
                }
            } catch (e: Exception) {
                // Handled
            }
        } else if (!isFacingQibla) {
            hasVibratedForCurrentAlignment = false
        }
    }

    // Animated glow transition
    val infiniteTransition = rememberInfiniteTransition(label = "qibla_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_anim"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Location & Distance Banner Card
        com.example.ui.components.LiquidGlassCard(
            shape = RoundedCornerShape(20.dp),
            glowColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            testTag = "qibla_location_card"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = profile.cityName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.4f° N, %.4f° E • %,d km to Kaaba",
                                profile.latitude,
                                profile.longitude,
                                distanceKm.roundToInt()
                            ),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh GPS",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Real-Time Qibla Bearing & Status Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isFacingQibla) {
                Color(0xFF1B5E20).copy(alpha = glowPulse * 0.95f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            },
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isFacingQibla) Color(0xFF4EE0A8) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("qibla_status_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isFacingQibla) Icons.Default.CheckCircle else Icons.Default.Navigation,
                    contentDescription = null,
                    tint = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFacingQibla) {
                        "✨ ALIGNED WITH QIBLA (${qiblaBearing.roundToInt()}° $cardinal) • FACING KAABA"
                    } else {
                        val turnDir = if (angleDifference > 0) "Turn ${abs(angleDifference).roundToInt()}° Right" else "Turn ${abs(angleDifference).roundToInt()}° Left"
                        "Qibla is at ${qiblaBearing.roundToInt()}° $cardinal • $turnDir"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tilt / Flat Surface Warning if needed
        AnimatedVisibility(visible = !sensorState.isFlatEnough) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF9800).copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hold your device flat on your hand or prayer mat for maximum compass accuracy.",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Compass Canvas Dial Component
        Box(
            modifier = Modifier
                .size(310.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow behind dial when aligned
            if (isFacingQibla) {
                Box(
                    modifier = Modifier
                        .size(290.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4EE0A8).copy(alpha = 0.4f * glowPulse),
                                    Color(0xFF1B5E20).copy(alpha = 0.15f * glowPulse),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Custom Drawn Compass Dial
            QiblaCompassDial(
                heading = sensorState.azimuth,
                qiblaBearing = qiblaBearing.toFloat(),
                isAligned = isFacingQibla,
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxSize()
            )

            // Center Info Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${sensorState.azimuth.roundToInt()}°",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isFacingQibla) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = QiblaCalculator.getCardinalDirection(sensorState.azimuth.toDouble()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Sensor Calibration / Status Pill
        com.example.ui.components.LiquidGlassCard(
            shape = RoundedCornerShape(18.dp),
            glowColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sensor Calibration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    val (accText, accColor) = when (sensorState.accuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High Precision" to Color(0xFF2E7D32)
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Good" to Color(0xFF1976D2)
                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low (Calibrate)" to Color(0xFFF57C00)
                        else -> "Uncalibrated" to Color(0xFFD32F2F)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = accText,
                            color = accColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (sensorState.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                        "⚠️ Magnetometer interference detected. Wave your phone smoothly in a figure-8 motion in the air to calibrate the compass sensor."
                    } else {
                        "Device rotation vector and magnetometer are active. Place phone flat and align the green arrow with the golden Kaaba mark."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        // Qibla Guidelines & Hadith Card
        com.example.ui.components.LiquidGlassCard(
            shape = RoundedCornerShape(18.dp),
            glowColor = Color(0xFF10B981),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Facing the Qibla (Al-Qiblah)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"So turn your face toward al-Masjid al-Haram. And wherever you [believers] are, turn your faces toward it in prayer.\" (Surah Al-Baqarah 2:144)\n\n" +
                            "Facing the Sacred Kaaba in Makkah is an essential prerequisite (Shart) for the validity of every obligatory and voluntary Salah.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun QiblaCompassDial(
    heading: Float,
    qiblaBearing: Float,
    isAligned: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val dialColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val qiblaAccentColor = if (isAligned) Color(0xFF00E676) else Color(0xFFFFD54F)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 12.dp.toPx()

        // 1. Draw outer background circle
        drawCircle(
            color = dialColor,
            radius = radius,
            center = center
        )

        drawCircle(
            color = outlineColor.copy(alpha = 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // 2. Rotate entire dial based on current device heading (-heading)
        rotate(-heading, pivot = center) {
            // Draw 360 degree ticks
            for (degree in 0 until 360 step 5) {
                val angleRad = Math.toRadians(degree.toDouble() - 90.0)
                val isMajor = degree % 30 == 0
                val isCardinal = degree % 90 == 0

                val tickLength = when {
                    isCardinal -> 14.dp.toPx()
                    isMajor -> 10.dp.toPx()
                    else -> 5.dp.toPx()
                }

                val strokeW = when {
                    isCardinal -> 2.5.dp.toPx()
                    isMajor -> 1.5.dp.toPx()
                    else -> 1.dp.toPx()
                }

                val tickColor = when {
                    degree == 0 -> Color(0xFFD32F2F) // North tick is Red
                    isCardinal -> primaryColor
                    isMajor -> onSurfaceColor.copy(alpha = 0.7f)
                    else -> outlineColor.copy(alpha = 0.4f)
                }

                val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // Draw Cardinal letters (N, E, S, W)
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 14.sp.toPx()
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            listOf(
                Pair(0, "N"),
                Pair(90, "E"),
                Pair(180, "S"),
                Pair(270, "W")
            ).forEach { (deg, label) ->
                val angleRad = Math.toRadians(deg.toDouble() - 90.0)
                val textRadius = radius - 26.dp.toPx()
                val x = center.x + textRadius * cos(angleRad).toFloat()
                val y = center.y + textRadius * sin(angleRad).toFloat() + 5.dp.toPx()

                paint.color = if (label == "N") {
                    android.graphics.Color.rgb(211, 47, 47)
                } else {
                    android.graphics.Color.rgb(
                        (primaryColor.red * 255).toInt(),
                        (primaryColor.green * 255).toInt(),
                        (primaryColor.blue * 255).toInt()
                    )
                }
                drawContext.canvas.nativeCanvas.drawText(label, x, y, paint)
            }

            // 3. Draw Qibla marker on the rotating dial rim
            val qiblaAngleRad = Math.toRadians(qiblaBearing.toDouble() - 90.0)
            val kaabaX = center.x + (radius - 10.dp.toPx()) * cos(qiblaAngleRad).toFloat()
            val kaabaY = center.y + (radius - 10.dp.toPx()) * sin(qiblaAngleRad).toFloat()

            // Gold / Green glowing Kaaba beacon
            drawCircle(
                color = qiblaAccentColor,
                radius = 9.dp.toPx(),
                center = Offset(kaabaX, kaabaY)
            )
            drawCircle(
                color = Color.Black,
                radius = 5.dp.toPx(),
                center = Offset(kaabaX, kaabaY)
            )

            // Line from center to Kaaba marker
            drawLine(
                color = qiblaAccentColor.copy(alpha = 0.5f),
                start = center,
                end = Offset(kaabaX, kaabaY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 4. Draw central Fixed Heading Pointer (Top Triangle pointing forward)
        val pointerPath = Path().apply {
            moveTo(center.x, center.y - radius - 8.dp.toPx())
            lineTo(center.x - 7.dp.toPx(), center.y - radius + 6.dp.toPx())
            lineTo(center.x + 7.dp.toPx(), center.y - radius + 6.dp.toPx())
            close()
        }
        drawPath(
            path = pointerPath,
            color = if (isAligned) Color(0xFF00E676) else Color(0xFFD32F2F)
        )

        // 5. Draw Center Dial Circle
        drawCircle(
            color = dialColor,
            radius = 44.dp.toPx(),
            center = center
        )
        drawCircle(
            color = if (isAligned) Color(0xFF00E676) else outlineColor.copy(alpha = 0.6f),
            radius = 44.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
