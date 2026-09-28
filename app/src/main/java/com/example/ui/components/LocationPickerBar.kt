package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.launch

@Composable
fun LocationPickerBar(
    profile: UserProfileEntity,
    isSyncingSearch: Boolean,
    onSyncSearchGrounding: () -> Unit,
    onUpdateLocation: (city: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("location_picker_bar"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEditDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = profile.cityName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "GPS: %.4f°N, %.4f°E", profile.latitude, profile.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(36.dp).testTag("edit_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditLocation,
                            contentDescription = "Change City",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onSyncSearchGrounding,
                        enabled = !isSyncingSearch,
                        modifier = Modifier.size(36.dp).testTag("sync_search_grounding_button")
                    ) {
                        if (isSyncingSearch) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync via Google Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grounding status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (profile.isGroundedViaSearch)
                            Color(0xFF0F5132).copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (profile.isGroundedViaSearch) Icons.Default.CheckCircle else Icons.Default.TravelExplore,
                    contentDescription = "Search Grounding Badge",
                    tint = if (profile.isGroundedViaSearch) Color(0xFF198754) else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (profile.isGroundedViaSearch)
                        "Google Search Grounded (gemini-3.5-flash)"
                    else
                        "GPS Astronomical Schedule (Tap refresh to sync with Search)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (profile.isGroundedViaSearch) Color(0xFF198754) else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    if (showEditDialog) {
        LocationEditDialog(
            currentCity = profile.cityName,
            currentLat = profile.latitude,
            currentLng = profile.longitude,
            onDismiss = { showEditDialog = false },
            onConfirm = { city, lat, lng ->
                onUpdateLocation(city, lat, lng)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun LocationEditDialog(
    currentCity: String,
    currentLat: Double,
    currentLng: Double,
    onDismiss: () -> Unit,
    onConfirm: (city: String, lat: Double, lng: Double) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var isDetectingGpsInDialog by remember { mutableStateOf(false) }

    var cityText by remember { mutableStateOf(currentCity) }
    var latText by remember { mutableStateOf(currentLat.toString()) }
    var lngText by remember { mutableStateOf(currentLng.toString()) }

    val presetLocations = listOf(
        Triple("Mumbai, India", 19.0760, 72.8777),
        Triple("New Delhi, India", 28.6139, 77.2090),
        Triple("Bengaluru, India", 12.9716, 77.5946),
        Triple("Hyderabad, India", 17.3850, 78.4867),
        Triple("Dubai, UAE", 25.2048, 55.2708),
        Triple("Mecca, Saudi Arabia", 21.3891, 39.8579),
        Triple("London, UK", 51.5074, -0.1278),
        Triple("New York, USA", 40.7128, -74.0060)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set GPS Location for Prayer Times", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Prayer timings will be grounded using Google Search data for this location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Detect GPS Button inside dialog
                Button(
                    onClick = {
                        isDetectingGpsInDialog = true
                        scope.launch {
                            val res = com.example.data.location.LocationHelper.detectCurrentLocation(context)
                            isDetectingGpsInDialog = false
                            if (res != null) {
                                cityText = res.cityName
                                latText = res.latitude.toString()
                                lngText = res.longitude.toString()
                                android.widget.Toast.makeText(context, "📍 Detected: ${res.cityName}", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Could not get GPS fix. Please turn on Location.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isDetectingGpsInDialog) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detecting Live GPS...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Detect Exact Current GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cityText,
                    onValueChange = { cityText = it },
                    label = { Text("City / Region") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("city_input_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Quick Preset Cities:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLocations.take(3).forEach { (city, lat, lng) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.clickable {
                                cityText = city
                                latText = lat.toString()
                                lngText = lng.toString()
                            }
                        ) {
                            Text(
                                text = city.split(",")[0],
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull() ?: currentLat
                    val lng = lngText.toDoubleOrNull() ?: currentLng
                    onConfirm(cityText.ifBlank { "Custom Location" }, lat, lng)
                },
                modifier = Modifier.testTag("save_location_button")
            ) {
                Text("Save & Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
