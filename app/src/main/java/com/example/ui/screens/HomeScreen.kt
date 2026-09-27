package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.ui.components.GoogleMapsCard
import com.example.ui.components.LocationPickerBar
import com.example.ui.components.NextPrayerCard
import com.example.ui.components.OverlayPermissionCard
import com.example.ui.components.PrayerTimeRow
import com.example.ui.viewmodel.MainUiState

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onOpenActiveLock: (PrayerType) -> Unit,
    onPrayerClick: (PrayerType) -> Unit,
    onSyncSearchGrounding: () -> Unit,
    onUpdateLocation: (city: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = uiState.schedule
    val profile = uiState.profile
    val nextPrayer = uiState.nextPrayer ?: PrayerType.FAJR

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display Over Other Apps Permission Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            OverlayPermissionCard()
        }

        item {
            LocationPickerBar(
                profile = profile,
                isSyncingSearch = uiState.isSyncingSearch,
                onSyncSearchGrounding = onSyncSearchGrounding,
                onUpdateLocation = onUpdateLocation
            )
        }

        item {
            NextPrayerCard(
                nextPrayer = uiState.nextPrayer,
                formattedTime = uiState.nextPrayerFormattedTime,
                secondsRemaining = uiState.secondsUntilNextPrayer,
                freeSkipsRemaining = profile.freeSkipsRemaining,
                currentStreak = profile.currentStreak
            )
        }

        // Active Lock Alert Card if in lock window
        if (uiState.isLockdownActive) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.fillMaxWidth().testTag("active_lockdown_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LOCKDOWN ACTIVE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Place Janamaz to authorize apps",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        ElevatedButton(
                            onClick = { onOpenActiveLock(uiState.currentLockdownPrayer ?: nextPrayer) },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open Lock", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Google Maps & Exact GPS Location Card
        item {
            GoogleMapsCard(
                profile = profile,
                onUpdateLocation = onUpdateLocation
            )
        }

        // Today's 5 Prayers Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Prayer Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "5 Prayers",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 5 Daily Prayers
        if (schedule != null) {
            items(schedule.prayers) { prayerItem ->
                PrayerTimeRow(
                    item = prayerItem,
                    isNextUpcoming = prayerItem.prayerType == uiState.nextPrayer,
                    onPrayerClick = { onPrayerClick(prayerItem.prayerType) }
                )
            }
        }

        // Daily Hadith / Reflection Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hadith of the Day",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"The first matter that the slave will be brought to account for on the Day of Judgment is the prayer. If it is sound, then the rest of his deeds will be sound.\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "— Sunan al-Tirmidhi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
