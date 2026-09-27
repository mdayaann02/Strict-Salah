package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PenaltyTransactionEntity
import com.example.data.local.PrayerLogEntity
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val profile = uiState.profile

    val offeredCount = uiState.allLogs.count { it.status == "OFFERED" }
    val skippedCount = uiState.allLogs.count { it.status == "SKIPPED" }
    val penaltyCount = uiState.allLogs.count { it.status == "PENALTY_PAID" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("history_screen")
    ) {
        // Summary Scorecards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Offered Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF198754).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF198754),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Offered", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF198754))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$offeredCount", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            // Free Skips Remaining Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (profile.freeSkipsRemaining > 0) Color(0xFFFFB300).copy(alpha = 0.15f) else Color(0xFFD32F2F).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (profile.freeSkipsRemaining > 0) Color(0xFFE5A800) else Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Free Quota", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (profile.freeSkipsRemaining > 0) Color(0xFFE5A800) else Color(0xFFD32F2F))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${profile.freeSkipsRemaining}/10", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            // Penalty Paid Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Fines Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "₹${uiState.totalPenaltiesCollected}", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row: Prayer Logs vs Penalty Transactions
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Prayer Logs (${uiState.allLogs.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("₹10 Penalty Ledger (${uiState.penaltyTransactions.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Prayer Logs list
            if (uiState.allLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No prayer logs yet. Offer your prayers to see verified Janamaz records here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.allLogs) { log ->
                        PrayerLogItemCard(log = log)
                    }
                }
            }
        } else {
            // ₹10 Penalty Transactions list
            if (uiState.penaltyTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No penalties paid! You still have free chances or offer prayers regularly.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.penaltyTransactions) { tx ->
                        PenaltyTransactionCard(transaction = tx)
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerLogItemCard(log: PrayerLogEntity) {
    val dateFormatted = remember(log.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(log.timestamp))
    }

    val bitmap = remember(log.photoBase64) {
        if (!log.photoBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(log.photoBase64, Base64.NO_WRAP)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail or status icon
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Janamaz thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (log.status) {
                                "OFFERED" -> Color(0xFF198754).copy(alpha = 0.2f)
                                "PENALTY_PAID" -> Color(0xFFD32F2F).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (log.status) {
                            "OFFERED" -> Icons.Default.CheckCircle
                            "PENALTY_PAID" -> Icons.Default.ReceiptLong
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (log.status) {
                            "OFFERED" -> Color(0xFF198754)
                            "PENALTY_PAID" -> Color(0xFFD32F2F)
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.prayerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (log.status) {
                            "OFFERED" -> Color(0xFF198754).copy(alpha = 0.2f)
                            "PENALTY_PAID" -> Color(0xFFD32F2F).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Text(
                            text = when (log.status) {
                                "OFFERED" -> "Verified (${log.aiConfidence}%)"
                                "PENALTY_PAID" -> "₹10 Fine Paid"
                                else -> "Skipped"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (log.status) {
                                "OFFERED" -> Color(0xFF198754)
                                "PENALTY_PAID" -> Color(0xFFD32F2F)
                                else -> MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormatted,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!log.aiExplanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.aiExplanation,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun PenaltyTransactionCard(transaction: PenaltyTransactionEntity) {
    val dateFormatted = remember(transaction.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(transaction.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹${transaction.amount}.00 Paid",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF198754).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "SUCCESS",
                            color = Color(0xFF198754),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "For ${transaction.prayerName} • ${transaction.paymentApp} • To: 8217317725@superyes",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Ref: ${transaction.upiRefId} • $dateFormatted",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
