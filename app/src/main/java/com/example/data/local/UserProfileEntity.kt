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
    val totalUninstallPenaltiesPaid: Int = 0, // Total ₹ paid for uninstallation unlocking
    val isUninstallUnlocked: Boolean = false, // If penalty paid, uninstallation clearance is active
    val uninstallUnlockExpiry: Long = 0L, // Expiry timestamp for uninstall permission
    val uninstallUnlockToken: String = "", // Secure token for uninstall authorization
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
    val ishaOffsetMinutes: Int = 0,
    val isGoogleSignedIn: Boolean = false,
    val googleEmail: String = "",
    val googleDisplayName: String = "",
    val googlePhotoUrl: String = "",
    val registeredJanamazUris: String = "",
    val isJanamazRegistered: Boolean = false,
    val customFajrTime: String = "",
    val customDhuhrTime: String = "",
    val customAsrTime: String = "",
    val customMaghribTime: String = "",
    val customIshaTime: String = "",
    val useCustomTimings: Boolean = false,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK", "AMOLED"
    val colorPalette: String = "EMERALD", // "EMERALD", "GOLD", "INDIGO", "CRIMSON", "DYNAMIC", "ROSE_GOLD"
    val userGender: String = "NEUTRAL", // "NEUTRAL", "BROTHER", "SISTER"
    val appLogoTheme: String = "DEFAULT", // "DEFAULT", "BROTHER", "SISTER"
    val firebaseUid: String = "",
    val authProvider: String = "LOCAL" // "GOOGLE", "FIREBASE", "LOCAL"
)
