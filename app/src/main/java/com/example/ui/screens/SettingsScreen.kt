package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.data.payment.UpiPaymentGateway
import com.example.notifications.StrictSalahAdminReceiver
import com.example.ui.components.PrayerTimingsManagementCard
import com.example.ui.components.RegisteredJanamazCard
import com.example.ui.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSaveOffsets: (fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) -> Unit,
    onSaveSettings: (notifEnabled: Boolean, reminderMin: Int, lockMin: Int, appLock: Boolean) -> Unit,
    onSetThemeMode: (String) -> Unit = {},
    onSetColorPalette: (String) -> Unit = {},
    onUpdateGenderAndLogo: (gender: String, logoTheme: String) -> Unit = { _, _ -> },
    onOpenVoluntaryPayment: () -> Unit = {},
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
    val context = LocalContext.current

    var fajrOffset by remember(profile.fajrOffsetMinutes) { mutableIntStateOf(profile.fajrOffsetMinutes) }
    var dhuhrOffset by remember(profile.dhuhrOffsetMinutes) { mutableIntStateOf(profile.dhuhrOffsetMinutes) }
    var asrOffset by remember(profile.asrOffsetMinutes) { mutableIntStateOf(profile.asrOffsetMinutes) }
    var maghribOffset by remember(profile.maghribOffsetMinutes) { mutableIntStateOf(profile.maghribOffsetMinutes) }
    var ishaOffset by remember(profile.ishaOffsetMinutes) { mutableIntStateOf(profile.ishaOffsetMinutes) }

    var notificationsEnabled by remember(profile.notificationsEnabled) { mutableStateOf(profile.notificationsEnabled) }
    var reminderMinutes by remember(profile.reminderMinutesBefore) { mutableIntStateOf(profile.reminderMinutesBefore) }
    var lockDuration by remember(profile.lockdownDurationMinutes) { mutableIntStateOf(profile.lockdownDurationMinutes) }
    var appLockEnabled by remember(profile.isAppLockServiceEnabled) { mutableStateOf(profile.isAppLockServiceEnabled) }

    // State map to manage which dropdown function menu is expanded
    // Defaults: Theme, Prayer Times and Lockdown open by default
    val expandedSections = remember {
        mutableStateMapOf(
            "TIMINGS" to false,
            "OFFSETS" to false,
            "LOCKDOWN" to true,
            "SECURITY" to false,
            "PAYMENT" to false,
            "THEME" to true,
            "JANAMAZ" to false,
            "NOTIFICATIONS" to false,
            "SYSTEM" to false
        )
    }

    val toggleSection = { key: String ->
        expandedSections[key] = !(expandedSections[key] ?: false)
    }

    val expandAll = {
        listOf("TIMINGS", "OFFSETS", "LOCKDOWN", "SECURITY", "PAYMENT", "THEME", "JANAMAZ", "NOTIFICATIONS", "SYSTEM").forEach {
            expandedSections[it] = true
        }
    }

    val collapseAll = {
        listOf("TIMINGS", "OFFSETS", "LOCKDOWN", "SECURITY", "PAYMENT", "THEME", "JANAMAZ", "NOTIFICATIONS", "SYSTEM").forEach {
            expandedSections[it] = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Control Header with Expand/Collapse All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Function Settings",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any category dropdown to expand",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { expandAll() },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.UnfoldMore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expand All", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { collapseAll() },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.UnfoldLess, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Collapse", fontSize = 10.sp)
                }
            }
        }

        // ==========================================
        // 1. DROPDOWN: THEME & LIQUID GLASS VISUALS
        // ==========================================
        SettingsDropdownCard(
            title = "Liquid Glass Theme & Visuals",
            subtitle = "Theme: ${profile.themeMode} • Palette: ${profile.colorPalette}",
            icon = Icons.Default.Palette,
            iconTint = Color(0xFF00B4D8),
            statusBadge = if (profile.themeMode == "LIQUID_GLASS") "Liquid Active" else profile.themeMode,
            isExpanded = expandedSections["THEME"] ?: false,
            onToggle = { toggleSection("THEME") },
            testTag = "dropdown_theme"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Choose your preferred theme style and accent color palette.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "THEME MODE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val themeModes = listOf(
                    Triple("LIQUID_GLASS", "Liquid", Icons.Default.WaterDrop),
                    Triple("SYSTEM", "System", Icons.Default.BrightnessAuto),
                    Triple("LIGHT", "Light", Icons.Default.LightMode),
                    Triple("DARK", "Dark", Icons.Default.DarkMode),
                    Triple("AMOLED", "AMOLED", Icons.Default.Bedtime)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    themeModes.forEach { (modeKey, label, icon) ->
                        val isSelected = profile.themeMode.equals(modeKey, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            onClick = { onSetThemeMode(modeKey) },
                            modifier = Modifier.weight(1f).testTag("theme_mode_$modeKey")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                    Triple("DYNAMIC", "Aqua Cyan", Color(0xFF00838F))
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
                                    .size(32.dp)
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = name.split(" ")[0],
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "APP LOGO & GENDER EDITION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.util.AppLogoTheme.entries.forEach { logoTheme ->
                        val isSelected = profile.appLogoTheme.equals(logoTheme.key, ignoreCase = true)
                        val themeAccent = when (logoTheme) {
                            com.example.util.AppLogoTheme.BROTHER -> Color(0xFF10B981)
                            com.example.util.AppLogoTheme.SISTER -> Color(0xFFF43F5E)
                            com.example.util.AppLogoTheme.DEFAULT -> Color(0xFF3B82F6)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) themeAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) themeAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val gender = when (logoTheme) {
                                        com.example.util.AppLogoTheme.BROTHER -> "BROTHER"
                                        com.example.util.AppLogoTheme.SISTER -> "SISTER"
                                        else -> "NEUTRAL"
                                    }
                                    onUpdateGenderAndLogo(gender, logoTheme.key)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = logoTheme.drawableRes),
                                        contentDescription = logoTheme.title,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = themeAccent,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.BottomEnd)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = when (logoTheme) {
                                        com.example.util.AppLogoTheme.DEFAULT -> "Universal"
                                        com.example.util.AppLogoTheme.BROTHER -> "Brother"
                                        com.example.util.AppLogoTheme.SISTER -> "Sister"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = themeAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = when (logoTheme) {
                                            com.example.util.AppLogoTheme.DEFAULT -> "Universal"
                                            com.example.util.AppLogoTheme.BROTHER -> "Masculine"
                                            com.example.util.AppLogoTheme.SISTER -> "Feminine"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeAccent,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. DROPDOWN: STRICT SALAH LOCKDOWN & ANTI-BYPASS
        // ==========================================
        SettingsDropdownCard(
            title = "Strict Salah Lockdown & Anti-Bypass",
            subtitle = if (appLockEnabled) "Active • $lockDuration min window • Full overlay guard" else "Disabled",
            icon = Icons.Default.Lock,
            iconTint = if (appLockEnabled) Color(0xFFD32F2F) else Color.Gray,
            statusBadge = if (appLockEnabled) "GUARD ON" else "OFF",
            isExpanded = expandedSections["LOCKDOWN"] ?: false,
            onToggle = { toggleSection("LOCKDOWN") },
            testTag = "dropdown_lockdown"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // App lock master switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enforce Strict Overlay Lockdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Locks screen automatically during prayer times until Janamaz photo is AI-verified or skip fine is paid.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = {
                            appLockEnabled = it
                            onSaveSettings(notificationsEnabled, reminderMinutes, lockDuration, it)
                        },
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                // Lockdown duration stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lock Window Duration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Time window where strict lock is strictly enforced after prayer start.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            onClick = {
                                if (lockDuration > 10) {
                                    lockDuration -= 5
                                    onSaveSettings(notificationsEnabled, reminderMinutes, lockDuration, appLockEnabled)
                                }
                            },
                            enabled = lockDuration > 10,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$lockDuration min",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        FilledIconButton(
                            onClick = {
                                if (lockDuration < 90) {
                                    lockDuration += 5
                                    onSaveSettings(notificationsEnabled, reminderMinutes, lockDuration, appLockEnabled)
                                }
                            },
                            enabled = lockDuration < 90,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. DROPDOWN: MOSQUE & CUSTOM PRAYER TIMINGS
        // ==========================================
        SettingsDropdownCard(
            title = "Mosque & Custom Prayer Timings",
            subtitle = if (profile.useCustomTimings) "Custom Mosque Jamat Timings" else "GPS Solar Timings",
            icon = Icons.Default.Schedule,
            iconTint = Color(0xFF0D5D44),
            statusBadge = if (profile.useCustomTimings) "Custom" else "GPS Baseline",
            isExpanded = expandedSections["TIMINGS"] ?: false,
            onToggle = { toggleSection("TIMINGS") },
            testTag = "dropdown_timings"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                PrayerTimingsManagementCard(
                    schedule = uiState.schedule,
                    profile = profile,
                    onUpdatePrayerTiming = onUpdatePrayerTiming,
                    onResetPrayerTiming = onResetPrayerTiming,
                    onResetAllToDefault = onResetAllPrayerTimings,
                    onToggleUseCustom = onToggleUseCustomTimings
                )
            }
        }

        // ==========================================
        // 4. DROPDOWN: PRAYER TIME OFFSETS & SOLAR GROUNDING
        // ==========================================
        SettingsDropdownCard(
            title = "Prayer Time Offsets & Solar Sync",
            subtitle = "Fajr: ${fajrOffset}m • Dhuhr: ${dhuhrOffset}m • Asr: ${asrOffset}m • Maghrib: ${maghribOffset}m • Isha: ${ishaOffset}m",
            icon = Icons.Default.Tune,
            iconTint = Color(0xFF825D00),
            statusBadge = if (profile.isGroundedViaSearch) "Search Grounded" else "GPS Grounded",
            isExpanded = expandedSections["OFFSETS"] ?: false,
            onToggle = { toggleSection("OFFSETS") },
            testTag = "dropdown_offsets"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Fine-tune individual prayer times by +/- minutes to match your local mosque Adhan timetable perfectly.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                val offsetItems = listOf(
                    Triple("Fajr Offset", fajrOffset) { v: Int -> fajrOffset = v },
                    Triple("Dhuhr Offset", dhuhrOffset) { v: Int -> dhuhrOffset = v },
                    Triple("Asr Offset", asrOffset) { v: Int -> asrOffset = v },
                    Triple("Maghrib Offset", maghribOffset) { v: Int -> maghribOffset = v },
                    Triple("Isha Offset", ishaOffset) { v: Int -> ishaOffset = v }
                )

                offsetItems.forEach { (name, value, setter) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledIconButton(
                                onClick = {
                                    setter(value - 1)
                                    onSaveOffsets(fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
                                },
                                modifier = Modifier.size(28.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                            }

                            Text(
                                text = "${if (value >= 0) "+$value" else "$value"} min",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(55.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            FilledIconButton(
                                onClick = {
                                    setter(value + 1)
                                    onSaveOffsets(fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
                                },
                                modifier = Modifier.size(28.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTriggerSearchSync,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_search_grounding_button")
                ) {
                    Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-Ground Timings via Google Search", fontSize = 12.sp)
                }
            }
        }

        // ==========================================
        // 5. DROPDOWN: DEVICE ADMIN & ANTI-UNINSTALL GUARD
        // ==========================================
        val adminComponent = remember { ComponentName(context, StrictSalahAdminReceiver::class.java) }
        val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
        val isDeviceAdminActive = remember(profile) { dpm.isAdminActive(adminComponent) }
        val isClearanceActive = profile.isUninstallUnlocked && (System.currentTimeMillis() < profile.uninstallUnlockExpiry)

        SettingsDropdownCard(
            title = "Device Admin & Anti-Uninstall Security",
            subtitle = if (isClearanceActive) "Clearance Pass Active" else "₹100 Uninstallation Penalty Policy Enforced",
            icon = Icons.Default.Shield,
            iconTint = if (isClearanceActive) Color(0xFF2E7D32) else Color(0xFFD32F2F),
            statusBadge = if (isClearanceActive) "UNLOCKED" else "LOCKED",
            isExpanded = expandedSections["SECURITY"] ?: false,
            onToggle = { toggleSection("SECURITY") },
            testTag = "dropdown_security"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isClearanceActive) Color(0xFF2E7D32).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (isClearanceActive) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (isClearanceActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isClearanceActive) {
                                "Uninstallation Clearance Pass is active! You can proceed to system settings to manage or remove the app."
                            } else {
                                "Strict Salah enforces a strict ₹100 discipline penalty to prevent uninstallation and foster lifelong Salah consistency."
                            },
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = if (isClearanceActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenDeRegistrationPledge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClearanceActive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_uninstall_penalty_button")
                ) {
                    Icon(
                        imageVector = if (isClearanceActive) Icons.Default.VerifiedUser else Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isClearanceActive) "View Clearance Pass / Token" else "Pay ₹100 Uninstallation Penalty Gateway",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // 6. DROPDOWN: UPI PAYMENT GATEWAY & PENALTIES
        // ==========================================
        SettingsDropdownCard(
            title = "UPI Payment Gateway & Penalties",
            subtitle = "Receiver: ${UpiPaymentGateway.OFFICIAL_UPI_ID} • Total: ₹${profile.totalPenaltiesPaid}",
            icon = Icons.Default.CurrencyRupee,
            iconTint = Color(0xFF1976D2),
            statusBadge = "₹10 / ₹100 UPI",
            isExpanded = expandedSections["PAYMENT"] ?: false,
            onToggle = { toggleSection("PAYMENT") },
            testTag = "dropdown_payment"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Official Payee UPI ID", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(UpiPaymentGateway.OFFICIAL_UPI_ID, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Payee: ${UpiPaymentGateway.PAYEE_NAME}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Prayer Skip Penalty", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹10.00", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Applied when 3 free chances end", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Uninstall Penalty", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹100.00", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("1-hr clearance token pass", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenVoluntaryPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_payment_app_redirect_button")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Redirect to Payment App (GPay / PhonePe / Paytm)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ==========================================
        // 7. DROPDOWN: JANAMAZ AI CAMERA & PROFILE
        // ==========================================
        SettingsDropdownCard(
            title = "Janamaz AI Camera Profile",
            subtitle = if (profile.isJanamazRegistered) "Janamaz Profile Registered" else "No Mat Profile Saved",
            icon = Icons.Default.CameraAlt,
            iconTint = Color(0xFF5F259F),
            statusBadge = if (profile.isJanamazRegistered) "AI Verified" else "Setup Needed",
            isExpanded = expandedSections["JANAMAZ"] ?: false,
            onToggle = { toggleSection("JANAMAZ") },
            testTag = "dropdown_janamaz"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                RegisteredJanamazCard(
                    profile = profile,
                    registeredBitmaps = uiState.registeredJanamazBitmaps,
                    onOpenRegistration = onOpenJanamazRegistration
                )
            }
        }

        // ==========================================
        // 8. DROPDOWN: ADHAN & NOTIFICATIONS
        // ==========================================
        SettingsDropdownCard(
            title = "Adhan & Prayer Notifications",
            subtitle = if (notificationsEnabled) "Alerts ON • $reminderMinutes min before Adhan" else "Alerts OFF",
            icon = Icons.Default.Notifications,
            iconTint = Color(0xFF00838F),
            statusBadge = if (notificationsEnabled) "ON" else "OFF",
            isExpanded = expandedSections["NOTIFICATIONS"] ?: false,
            onToggle = { toggleSection("NOTIFICATIONS") },
            testTag = "dropdown_notifications"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Salah Time Notifications", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Get timely Adhan reminders before every Salah.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pre-Salah Reminder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Minutes before Adhan to send preparatory alert.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledIconButton(
                            onClick = {
                                if (reminderMinutes > 5) {
                                    reminderMinutes -= 5
                                    onSaveSettings(notificationsEnabled, reminderMinutes, lockDuration, appLockEnabled)
                                }
                            },
                            enabled = reminderMinutes > 5,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text("$reminderMinutes min", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        FilledIconButton(
                            onClick = {
                                if (reminderMinutes < 30) {
                                    reminderMinutes += 5
                                    onSaveSettings(notificationsEnabled, reminderMinutes, lockDuration, appLockEnabled)
                                }
                            },
                            enabled = reminderMinutes < 30,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // 9. DROPDOWN: STRICT SALAH V2.0 SYSTEM INFO
        // ==========================================
        SettingsDropdownCard(
            title = "Strict Salah v2.0 System Information",
            subtitle = "Version 2.0 • Firebase Auth & Gender Customization",
            icon = Icons.Default.Info,
            iconTint = MaterialTheme.colorScheme.primary,
            statusBadge = "v2.0 Build",
            isExpanded = expandedSections["SYSTEM"] ?: false,
            onToggle = { toggleSection("SYSTEM") },
            testTag = "dropdown_system"
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Strict Salah v2.0 is engineered with Liquid Glass dynamic fluid UI, Firebase Auth, Google Sign-In, multi-gender dynamic app logo customization, and robust UPI payment redirection.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Version: 2.0 (Official Personalized Release)\n• Authentication: Firebase Auth & Google Sign-In\n• Official Payee UPI: 8217317725@superyes\n• Storage: Encrypted Offline Room Database & Google Drive Sync",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * High-end Dropdown Menu / Accordion Card with Liquid Glass styling and smooth chevron animation
 */
@Composable
fun SettingsDropdownCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    statusBadge: String? = null,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) iconTint.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column {
            // Clickable Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (statusBadge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = iconTint.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconTint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            // Animated Collapsible Content Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = 1.dp
                    )
                    content()
                }
            }
        }
    }
}
