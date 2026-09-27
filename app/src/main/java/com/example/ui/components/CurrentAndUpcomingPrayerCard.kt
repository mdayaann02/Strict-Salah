package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailySchedule
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import java.util.Locale

@Composable
fun CurrentAndUpcomingPrayerCard(
    schedule: DailySchedule?,
    nextPrayer: PrayerType,
    nextPrayerFormattedTime: String,
    secondsUntilNextPrayer: Long,
    isLockdownActive: Boolean,
    currentLockdownPrayer: PrayerType?,
    currentStreak: Int,
    freeSkipsRemaining: Int,
    onOpenLockdown: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine current prayer window if any
    val prayers = schedule?.prayers ?: emptyList()
    val nextItem = prayers.firstOrNull { it.prayerType == nextPrayer }

    // Find the current active prayer (e.g. either locked active or current in chronological order)
    val currentActiveItem: PrayerTimeItem? = prayers.firstOrNull { it.isLockedActive }
        ?: run {
            // Find the prayer immediately before nextPrayer
            val nextIndex = prayers.indexOfFirst { it.prayerType == nextPrayer }
            if (nextIndex > 0) prayers[nextIndex - 1] else prayers.lastOrNull()
        }

    val hours = secondsUntilNextPrayer / 3600
    val minutes = (secondsUntilNextPrayer % 3600) / 60
    val secs = secondsUntilNextPrayer % 60
    val countdownStr = if (hours > 0) {
        String.format(Locale.US, "%dh %02dm %02ds", hours, minutes, secs)
    } else {
        String.format(Locale.US, "%02dm %02ds", minutes, secs)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_and_upcoming_prayer_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                // Top row: Badges for Streak and Skips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFF6D00).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentStreak Day Streak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFF6D00)
                            )
                        }
                    }

                    // Free Skips Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (freeSkipsRemaining > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFD32F2F).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (freeSkipsRemaining > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color(0xFFD32F2F).copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (freeSkipsRemaining > 0) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$freeSkipsRemaining Free Skip${if (freeSkipsRemaining == 1) "" else "s"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (freeSkipsRemaining > 0) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CURRENT & UPCOMING SPLIT SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CURRENT NAMAZ BOX
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CURRENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentActiveItem?.prayerType?.displayName ?: "Fajr",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentActiveItem?.timeFormatted ?: "--:--",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val isCurrentOffered = currentActiveItem?.isOfferedToday == true
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrentOffered) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFF57F17).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isCurrentOffered) "✓ Offered" else "• Pending",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentOffered) Color(0xFF2E7D32) else Color(0xFFF57F17),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // UPCOMING NAMAZ BOX
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UPCOMING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = nextPrayer.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = nextPrayerFormattedTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "in $countdownStr",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Active Lockdown Banner inside Card if triggered
                AnimatedVisibility(visible = isLockdownActive) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.fillMaxWidth().testTag("active_lockdown_hero_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "LOCKDOWN ACTIVE",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Scan Janamaz Mat to unlock apps",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                ElevatedButton(
                                    onClick = { onOpenLockdown(currentLockdownPrayer ?: nextPrayer) },
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFFD32F2F)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Verify", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
