package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.UserProfileEntity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GoogleMapsCard(
    profile: UserProfileEntity,
    onUpdateLocation: (city: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDetectingLocation by remember { mutableStateOf(false) }

    // Calculate Qibla bearing towards Mecca (Kaaba: 21.4225° N, 39.8262° E)
    val qiblaBearing = remember(profile.latitude, profile.longitude) {
        calculateQiblaBearing(profile.latitude, profile.longitude)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun reverseGeocodeAndSave(lat: Double, lng: Double) {
        scope.launch(Dispatchers.IO) {
            var cityName = "Detected Location"
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                            val country = addr.countryName ?: ""
                            cityName = if (country.isNotBlank()) "$locality, $country" else locality
                        }
                        scope.launch(Dispatchers.Main) {
                            onUpdateLocation(cityName, lat, lng)
                            isDetectingLocation = false
                            Toast.makeText(context, "📍 Exact GPS updated via Google Maps: $cityName", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@launch
                } else {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                        val country = addr.countryName ?: ""
                        cityName = if (country.isNotBlank()) "$locality, $country" else locality
                    }
                }
            } catch (e: Exception) {
                cityName = String.format(Locale.US, "GPS (%.3f, %.3f)", lat, lng)
            }
            withContext(Dispatchers.Main) {
                onUpdateLocation(cityName, lat, lng)
                isDetectingLocation = false
                Toast.makeText(context, "📍 Exact GPS updated: $cityName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchCurrentGpsLocation() {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED) {
            isDetectingLocation = true
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        reverseGeocodeAndSave(location.latitude, location.longitude)
                    } else {
                        // Fallback to last known location
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                reverseGeocodeAndSave(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                isDetectingLocation = false
                                Toast.makeText(context, "Could not acquire GPS fix. Please turn on device Location.", Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener {
                            isDetectingLocation = false
                            Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    isDetectingLocation = false
                    Toast.makeText(context, "GPS error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentGpsLocation()
        } else {
            Toast.makeText(context, "Location permission is needed for Google Maps prayer time accuracy", Toast.LENGTH_LONG).show()
        }
    }

    fun requestLocationAndDetect() {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (finePerm == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun openInGoogleMaps() {
        try {
            val uri = Uri.parse("geo:${profile.latitude},${profile.longitude}?q=${profile.latitude},${profile.longitude}(Strict+Namaz+Current+Location)&z=16")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${profile.latitude},${profile.longitude}")
                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${profile.latitude},${profile.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    fun findMosquesNearby() {
        try {
            val uri = Uri.parse("geo:${profile.latitude},${profile.longitude}?q=mosque")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/search/mosque/@${profile.latitude},${profile.longitude},14z")
                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/search/mosque/@${profile.latitude},${profile.longitude},14z")
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_maps_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA4335).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Google Maps Pin",
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Maps & GPS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exact coordinates for prayer & Qibla",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Detect GPS Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { requestLocationAndDetect() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Detect GPS",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDetectingLocation) "Locating..." else "Detect GPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Map Simulation Canvas with GPS Pin and Radar Rings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2A38))
                    .border(1.dp, Color(0xFF334E68), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)

                    // Grid lines resembling street maps
                    val strokeGrid = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    drawLine(Color(0xFF2C3E50), Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 1.dp.toPx())
                    drawLine(Color(0xFF2C3E50), Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 1.dp.toPx())
                    drawLine(Color(0xFF2C3E50), Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 1.dp.toPx())
                    drawLine(Color(0xFF2C3E50), Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 1.dp.toPx())

                    // Radar pulse circles around user location
                    drawCircle(Color(0xFF4285F4).copy(alpha = 0.15f), radius = 45.dp.toPx(), center = center)
                    drawCircle(Color(0xFF4285F4).copy(alpha = 0.25f), radius = 28.dp.toPx(), center = center)
                    drawCircle(Color(0xFF4285F4), radius = 6.dp.toPx(), center = center)
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = center)
                }

                // Overlay Map Details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = profile.cityName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = String.format(Locale.US, "GPS: %.4f° N, %.4f° E", profile.latitude, profile.longitude),
                        color = Color(0xFF90CAF9),
                        fontSize = 11.sp
                    )
                }

                // Qibla Direction Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Qibla Direction",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Qibla: $qiblaBearing°",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Open in Google Maps & Find Mosques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openInGoogleMaps() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_google_maps_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View on Maps", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { findMosquesNearby() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("find_mosques_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B5E20)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nearby Mosques", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Calculates the exact Qibla compass bearing towards the Kaaba (21.4225° N, 39.8262° E)
 * using the great-circle spherical forward azimuth formula.
 */
fun calculateQiblaBearing(lat: Double, lng: Double): Int {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLng = Math.toRadians(39.8262)
    val userLat = Math.toRadians(lat)
    val userLng = Math.toRadians(lng)

    val deltaLng = kaabaLng - userLng
    val y = sin(deltaLng) * cos(kaabaLat)
    val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(deltaLng)

    val bearingRad = atan2(y, x)
    var bearingDeg = Math.toDegrees(bearingRad)
    bearingDeg = (bearingDeg + 360) % 360
    return bearingDeg.toInt()
}
