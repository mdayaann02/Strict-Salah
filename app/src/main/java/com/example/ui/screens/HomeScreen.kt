package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.data.qibla.QiblaCalculator
import com.example.ui.components.CurrentAndUpcomingPrayerCard
import com.example.ui.components.GoogleMapsCard
import com.example.ui.components.HadithCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LocationPickerBar
import com.example.ui.components.NamazStatisticsDialog
import com.example.ui.components.OverlayPermissionCard
import com.example.ui.components.PrayerTimeRow
import com.example.ui.theme.LiquidAqua
import com.example.ui.viewmodel.MainUiState
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onOpenActiveLock: (PrayerType) -> Unit,
    onPrayerClick: (PrayerType) -> Unit,
    onOpenQibla: () -> Unit = {},
    onSyncSearchGrounding: () -> Unit,
    onUpdateLocation: (city: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = uiState.schedule
    val profile = uiState.profile
    val nextPrayer = uiState.nextPrayer ?: PrayerType.FAJR
    var selectedPrayerForStats by remember { mutableStateOf<PrayerType?>(null) }

    val qiblaBearing = remember(profile.latitude, profile.longitude) {
        QiblaCalculator.calculateQiblaBearing(profile.latitude, profile.longitude)
    }
    val cardinal = remember(qiblaBearing) {
        QiblaCalculator.getCardinalDirection(qiblaBearing)
    }

    selectedPrayerForStats?.let { selectedPrayer ->
        val scheduledTime = schedule?.prayers?.firstOrNull { it.prayerType == selectedPrayer }?.timeFormatted ?: "--:--"
        val logsForThisPrayer = uiState.allLogs.filter { it.prayerName.equals(selectedPrayer.name, ignoreCase = true) }
        NamazStatisticsDialog(
            prayerType = selectedPrayer,
            scheduledTimeFormatted = scheduledTime,
            logsForThisPrayer = logsForThisPrayer,
            currentStreak = profile.currentStreak,
            onDismiss = { selectedPrayerForStats = null }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overlay Permission Banner if needed
        item {
            Spacer(modifier = Modifier.height(4.dp))
            OverlayPermissionCard()
        }

        // 1. FIRST: CURRENT SALAH AND UPCOMING SALAH HERO CARD
        item {
            CurrentAndUpcomingPrayerCard(
                schedule = schedule,
                nextPrayer = nextPrayer,
                nextPrayerFormattedTime = uiState.nextPrayerFormattedTime,
                secondsUntilNextPrayer = uiState.secondsUntilNextPrayer,
                isLockdownActive = uiState.isLockdownActive,
                currentLockdownPrayer = uiState.currentLockdownPrayer,
                currentStreak = profile.currentStreak,
                freeSkipsRemaining = profile.freeSkipsRemaining,
                onOpenLockdown = onOpenActiveLock
            )
        }

        // 2. QUICK QIBLA FINDER BANNER
        item {
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                glowColor = Color(0xFF10B981),
                onClick = { onOpenQibla() },
                testTag = "home_qibla_quick_card",
                modifier = Modifier.fillMaxWidth()
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Qibla Direction",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Qibla Direction",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1B5E20).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${qiblaBearing.roundToInt()}° $cardinal",
                                        color = Color(0xFF1B5E20),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Align your Janamaz to Holy Kaaba in Makkah",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open Compass",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. TODAY'S PRAYER SCHEDULE (12-HOUR FORMAT)
        item {
            LiquidGlassCard(
                shape = RoundedCornerShape(22.dp),
                glowColor = LiquidAqua,
                testTag = "prayer_schedule_card",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Today's Salah Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = schedule?.sourceDescription ?: "12-Hour Daily Times",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "12h Format",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    schedule?.prayers?.forEachIndexed { index, prayerItem ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        PrayerTimeRow(
                            item = prayerItem,
                            isNextUpcoming = prayerItem.prayerType == nextPrayer,
                            onPrayerClick = { selectedPrayerForStats = prayerItem.prayerType }
                        )
                    }
                }
            }
        }

        // 4. GPS LOCATION & GOOGLE SEARCH GROUNDING
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LocationPickerBar(
                    profile = profile,
                    isSyncingSearch = uiState.isSyncingSearch,
                    onSyncSearchGrounding = onSyncSearchGrounding,
                    onUpdateLocation = onUpdateLocation
                )

                GoogleMapsCard(
                    profile = profile,
                    onUpdateLocation = onUpdateLocation
                )
            }
        }

        // 5. HADITH OF THE DAY
        item {
            HadithCard()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
