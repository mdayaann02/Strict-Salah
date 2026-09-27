package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val freeSkipsRemaining: Int = 10, // Strictly 10 chances initially
    val initialFreeSkips: Int = 10,
    val totalPenaltiesPaid: Int = 0, // Sum in INR ₹
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val cityName: String = "Detecting GPS...",
    val latitude: Double = 19.0760, // Default to Mumbai or dynamic GPS
    val longitude: Double = 72.8777,
    val notificationsEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 10,
    val lockdownDurationMinutes: Int = 25,
    val isAppLockServiceEnabled: Boolean = true,
    val lastGroundingSyncTime: Long = 0L,
    val isGroundedViaSearch: Boolean = false,
    val fajrOffsetMinutes: Int = 0,
    val dhuhrOffsetMinutes: Int = 0,
    val asrOffsetMinutes: Int = 0,
    val maghribOffsetMinutes: Int = 0,
    val ishaOffsetMinutes: Int = 0
)
