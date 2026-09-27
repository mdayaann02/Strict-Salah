package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.ui.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSaveOffsets: (fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) -> Unit,
    onSaveSettings: (notifEnabled: Boolean, reminderMin: Int, lockMin: Int, appLock: Boolean) -> Unit,
    onTriggerSearchSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile

    var fajrOffset by remember(profile.fajrOffsetMinutes) { mutableIntStateOf(profile.fajrOffsetMinutes) }
    var dhuhrOffset by remember(profile.dhuhrOffsetMinutes) { mutableIntStateOf(profile.dhuhrOffsetMinutes) }
    var asrOffset by remember(profile.asrOffsetMinutes) { mutableIntStateOf(profile.asrOffsetMinutes) }
    var maghribOffset by remember(profile.maghribOffsetMinutes) { mutableIntStateOf(profile.maghribOffsetMinutes) }
    var ishaOffset by remember(profile.ishaOffsetMinutes) { mutableIntStateOf(profile.ishaOffsetMinutes) }

    var notificationsEnabled by remember(profile.notificationsEnabled) { mutableStateOf(profile.notificationsEnabled) }
    var reminderMinutes by remember(profile.reminderMinutesBefore) { mutableIntStateOf(profile.reminderMinutesBefore) }
    var lockDuration by remember(profile.lockdownDurationMinutes) { mutableIntStateOf(profile.lockdownDurationMinutes) }
    var appLockEnabled by remember(profile.isAppLockServiceEnabled) { mutableStateOf(profile.isAppLockServiceEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Settings Section
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications & Reminders",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prayer Alerts", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Timely notification before and at Namaz start",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            onSaveSettings(it, reminderMinutes, lockDuration, appLockEnabled)
                        },
                        modifier = Modifier.testTag("notification_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder minutes selector
                Text("Reminder Timing Before Prayer:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5, 10, 15).forEach { min ->
                        val isSelected = reminderMinutes == min
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = {
                                reminderMinutes = min
                                onSaveSettings(notificationsEnabled, min, lockDuration, appLockEnabled)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$min mins",
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Custom Prayer Timings Offset Section
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fine-Tune Prayer Timings (Offsets)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Adjust individual prayer timings by minutes to match your local masjid azan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OffsetAdjusterRow(name = "Fajr", offset = fajrOffset) { fajrOffset = it }
                OffsetAdjusterRow(name = "Dhuhr", offset = dhuhrOffset) { dhuhrOffset = it }
                OffsetAdjusterRow(name = "Asr", offset = asrOffset) { asrOffset = it }
                OffsetAdjusterRow(name = "Maghrib", offset = maghribOffset) { maghribOffset = it }
                OffsetAdjusterRow(name = "Isha", offset = ishaOffset) { ishaOffset = it }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onSaveOffsets(fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_offsets_button")
                ) {
                    Text("Save Custom Offsets")
                }
            }
        }

        // Google Search Grounding Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Search Grounding",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Namaz Lock integrates gemini-3.5-flash with googleSearch tool to fetch authentic daily prayer timings for your exact GPS coordinates (${profile.cityName}).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onTriggerSearchSync,
                    enabled = !uiState.isSyncingSearch,
                    modifier = Modifier.fillMaxWidth().testTag("sync_search_button")
                ) {
                    Text(if (uiState.isSyncingSearch) "Grounding via Google Search..." else "Re-sync with Google Search Now")
                }
            }
        }

        // Leave Namaz & Penalty Rules
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFFE5A800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "10 Chances & ₹10 Penalty Rule",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Every user begins with exactly 10 free chances to leave or skip Namaz.\n" +
                            "• Once all 10 chances are exhausted, skipping a prayer strictly incurs a penalty fee of ₹10 per namaz.\n" +
                            "• Free Skips Remaining: ${profile.freeSkipsRemaining} / 10\n" +
                            "• Total Fines Paid: ₹${uiState.totalPenaltiesCollected}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OffsetAdjusterRow(
    name: String,
    offset: Int,
    onOffsetChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = { onOffsetChanged(offset - 1) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
            }

            Text(
                text = if (offset >= 0) "+$offset min" else "$offset min",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(68.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            FilledIconButton(
                onClick = { onOffsetChanged(offset + 1) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
            }
        }
    }
}
