package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val prayerName: String, // FAJR, DHUHR, etc.
    val status: String, // "OFFERED", "SKIPPED", "PENALTY_PAID"
    val timestamp: Long = System.currentTimeMillis(),
    val photoBase64: String? = null,
    val aiConfidence: Int = 0,
    val aiExplanation: String? = null,
    val penaltyAmount: Int = 0, // ₹0 or ₹10
    val transactionRef: String? = null
)
