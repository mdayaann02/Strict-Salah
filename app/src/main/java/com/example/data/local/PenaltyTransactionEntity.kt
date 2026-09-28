package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "penalty_transactions")
data class PenaltyTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String,
    val prayerName: String = "N/A",
    val transactionType: String = "PRAYER_SKIP", // "UNINSTALL_PENALTY" or "PRAYER_SKIP"
    val amount: Int = 10, // ₹10 for skip, ₹100 for uninstallation penalty
    val upiRefId: String,
    val paymentApp: String, // Google Pay, PhonePe, Paytm, BHIM UPI, CRED, Amazon Pay
    val paymentStatus: String = "SUCCESS",
    val receiptNumber: String = "SS-TXN-${System.currentTimeMillis() % 1000000}",
    val remarks: String = "Discipline fee paid via UPI"
)
