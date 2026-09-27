package com.example.data.model

data class PrayerTimeItem(
    val prayerType: PrayerType,
    val timeFormatted: String, // HH:mm format, e.g. "05:15"
    val isLockedActive: Boolean = false,
    val isOfferedToday: Boolean = false,
    val isSkippedToday: Boolean = false,
    val isPenaltyPaid: Boolean = false
)

data class DailySchedule(
    val dateString: String,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val isGroundedWithGoogleSearch: Boolean = false,
    val sourceDescription: String = "Calculated via GPS coordinates",
    val prayers: List<PrayerTimeItem>
)

data class VerificationResult(
    val isJanamaz: Boolean,
    val confidence: Int, // 0 to 100
    val summary: String,
    val details: String,
    val orientationValid: Boolean = true,
    val cleanSettingDetected: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
