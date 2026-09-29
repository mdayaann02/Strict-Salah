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
import com.example.ui.theme.LiquidAqua
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

    LiquidGlassSurface(
        shape = RoundedCornerShape(26.dp),
        glowColor = if (isLockdownActive) Color(0xFFEF4444) else LiquidAqua,
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_and_upcoming_prayer_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF9100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentStreak Day Streak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFF9100)
                            )
                        }
                    }

                    // Free Skips Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (freeSkipsRemaining > 0) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFFD32F2F).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (freeSkipsRemaining > 0) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFFD32F2F).copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (freeSkipsRemaining > 0) Color(0xFF00E5FF) else Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$freeSkipsRemaining / 3 Free Skips",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (freeSkipsRemaining > 0) Color(0xFF00E5FF) else Color(0xFFFF5252)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CURRENT & UPCOMING SPLIT SECTION (Apple Music Frosted Containers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CURRENT NAMAZ BOX
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CURRENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                AppleMusicWaveVisualizer(color = Color(0xFF10B981))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentActiveItem?.prayerType?.displayName ?: "Fajr",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = currentActiveItem?.timeFormatted ?: "--:--",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val isCurrentOffered = currentActiveItem?.isOfferedToday == true
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrentOffered) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFFF59E0B).copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrentOffered) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (isCurrentOffered) "✓ Offered" else "• Pending",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentOffered) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // UPCOMING NAMAZ BOX
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.30f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UPCOMING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = nextPrayer.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = nextPrayerFormattedTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "in $countdownStr",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DD3FC),
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
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFDC2626),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth().testTag("active_lockdown_hero_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
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
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "LOCKDOWN ACTIVE",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            letterSpacing = 1.sp
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
                                        contentColor = Color(0xFFDC2626)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
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
