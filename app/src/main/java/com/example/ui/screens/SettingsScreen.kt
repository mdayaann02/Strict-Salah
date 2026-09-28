package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.data.model.PrayerType
import com.example.ui.components.PrayerTimingsManagementCard
import com.example.ui.components.RegisteredJanamazCard
import com.example.ui.viewmodel.MainUiState

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.ui.platform.LocalContext
import com.example.notifications.StrictSalahAdminReceiver

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSaveOffsets: (fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) -> Unit,
    onSaveSettings: (notifEnabled: Boolean, reminderMin: Int, lockMin: Int, appLock: Boolean) -> Unit,
    onSetThemeMode: (String) -> Unit = {},
    onSetColorPalette: (String) -> Unit = {},
    onTriggerSearchSync: () -> Unit,
    onOpenJanamazRegistration: () -> Unit = {},
    onOpenDeRegistrationPledge: () -> Unit = {},
    onUpdatePrayerTiming: (PrayerType, String) -> Unit = { _, _ -> },
    onResetPrayerTiming: (PrayerType) -> Unit = {},
    onResetAllPrayerTimings: () -> Unit = {},
    onToggleUseCustomTimings: (Boolean) -> Unit = {},
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
        Spacer(modifier = Modifier.height(4.dp))

        // 1. APP THEME & APPEARANCE CUSTOMIZATION CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("app_theme_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "App Theme & Appearance",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose your preferred theme mode (including pitch-black AMOLED) and dynamic accent color palette.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Mode Selector (System, Light, Dark, AMOLED)
                Text(
                    text = "THEME MODE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val themeModes = listOf(
                    Triple("SYSTEM", "System", Icons.Default.BrightnessAuto),
                    Triple("LIGHT", "Light", Icons.Default.LightMode),
                    Triple("DARK", "Dark", Icons.Default.DarkMode),
                    Triple("AMOLED", "AMOLED", Icons.Default.Bedtime)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themeModes.forEach { (modeKey, label, icon) ->
                        val isSelected = profile.themeMode.equals(modeKey, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            onClick = { onSetThemeMode(modeKey) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_$modeKey")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Accent Color Palette Selector
                Text(
                    text = "ACCENT COLOR PALETTE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val palettes = listOf(
                    Triple("EMERALD", "Emerald", Color(0xFF0D5D44)),
                    Triple("GOLD", "Amber Gold", Color(0xFF825D00)),
                    Triple("INDIGO", "Indigo", Color(0xFF1E5BB0)),
                    Triple("CRIMSON", "Crimson", Color(0xFF9E2A2B)),
                    Triple("DYNAMIC", "Dynamic", Color(0xFF00838F))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palettes.forEach { (paletteKey, name, color) ->
                        val isSelected = profile.colorPalette.equals(paletteKey, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onSetColorPalette(paletteKey) }
                                .padding(4.dp)
                                .testTag("color_palette_$paletteKey")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. 12-Hour Prayer Timings & Mosque Schedule Management Card
        PrayerTimingsManagementCard(
            schedule = uiState.schedule,
            profile = profile,
            onUpdatePrayerTiming = onUpdatePrayerTiming,
            onResetPrayerTiming = onResetPrayerTiming,
            onResetAllToDefault = onResetAllPrayerTimings,
            onToggleUseCustom = onToggleUseCustomTimings
        )

        // 3. Notification Settings Section
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
                        Text("Salah Alerts", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Timely notification before and at Salah start",
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

        // 4. Custom Prayer Timings Offset Section
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

        // 5. Google Search Grounding Card
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
                    text = "Strict Salah integrates gemini-3.5-flash with googleSearch tool to fetch authentic daily prayer timings for your exact GPS coordinates (${profile.cityName}).",
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

        // 6. Leave Salah & Penalty Rules
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
                    text = "• Every user begins with exactly 10 free chances to leave or skip Salah.\n" +
                            "• Once all 10 chances are exhausted, skipping a prayer strictly incurs a penalty fee of ₹10 per salah.\n" +
                            "• Free Skips Remaining: ${profile.freeSkipsRemaining} / 10\n" +
                            "• Total Fines Paid: ₹${uiState.totalPenaltiesCollected}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // 7. Registered Janamaz Mat Configuration Card
        RegisteredJanamazCard(
            profile = profile,
            registeredBitmaps = uiState.registeredJanamazBitmaps,
            onOpenRegistration = onOpenJanamazRegistration
        )

        // 8. Strict Salah v3.0 Anti-Close & ₹100 Uninstallation Penalty Card
        val context = LocalContext.current
        val adminComponent = remember { ComponentName(context, StrictSalahAdminReceiver::class.java) }
        val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager }
        val isAdminActive = dpm?.isAdminActive(adminComponent) == true
        val isUnlocked = profile.isUninstallUnlocked && (profile.uninstallUnlockExpiry == 0L || System.currentTimeMillis() < profile.uninstallUnlockExpiry)

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color(0xFFD32F2F).copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth().testTag("anti_close_and_uninstall_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.Shield else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Strict Salah v3.0 Uninstallation Guard",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Active Foreground Enforcement prevents closing or bypassing prayer lockdowns from recent applications.\n" +
                            "• Any attempt to swipe the app away during Salah window immediately triggers automatic relaunch.\n" +
                            "• Uninstallation requires an official ₹100 discipline penalty sent via secure UPI to 8217317725@superyes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Clearance status badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.12f) else Color(0xFFD32F2F).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isUnlocked) "🔓 Uninstallation Clearance Active" else "🔒 Uninstallation Strictly Protected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                            Text(
                                text = if (isUnlocked)
                                    "Clearance Pass: ${profile.uninstallUnlockToken.ifBlank { "ACTIVE" }}"
                                else
                                    "₹100 Penalty fee required before uninstallation",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Device Admin Protection Toggle / Setup
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Device Admin Anti-Tamper",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isAdminActive) "🛡️ Device Admin Active (Blocks casual uninstallation)" else "Enable Device Admin to strictly prevent uninstall bypass",
                                fontSize = 10.sp,
                                color = if (isAdminActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isAdminActive) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Strict Salah requires Device Admin to enforce active prayer lockdowns.")
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("enable_device_admin_button")
                            ) {
                                Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ₹100 Uninstall Penalty / Unlock Button
                Button(
                    onClick = onOpenDeRegistrationPledge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_deregistration_pledge_button")
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.Shield else Icons.Default.LockPerson,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUnlocked) "Manage Clearance / Open App Settings" else "Pay ₹100 UPI Penalty to Unlock Uninstallation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 9. App Version Info Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Strict Salah v3.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Secure UPI Payment Gateway • Qibla Compass Finder • AMOLED Theme • AI Janamaz Verification",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
