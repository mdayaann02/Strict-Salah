package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "penalty_transactions")
data class PenaltyTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String,
    val prayerName: String,
    val amount: Int = 10, // ₹10 per skipped namaz
    val upiRefId: String,
    val paymentApp: String, // Google Pay, PhonePe, Paytm, BHIM UPI
    val paymentStatus: String = "SUCCESS",
    val remarks: String = "Penalty fee for skipping prayer without remaining free quota"
)
