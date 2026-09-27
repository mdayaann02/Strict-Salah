package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector

enum class PrayerType(
    val id: String,
    val displayName: String,
    val arabicName: String,
    val defaultTime: String,
    val rakats: Int,
    val description: String
) {
    FAJR(
        id = "fajr",
        displayName = "Fajr",
        arabicName = "الفجر",
        defaultTime = "05:15",
        rakats = 2,
        description = "Dawn prayer offered before sunrise. The Prophet (PBUH) said: 'The two Sunnah rak'ahs of Fajr are better than the world and all it contains.'"
    ),
    DHUHR(
        id = "dhuhr",
        displayName = "Dhuhr",
        arabicName = "الظهر",
        defaultTime = "12:30",
        rakats = 4,
        description = "Noon prayer offered after true midday when the sun begins its decline."
    ),
    ASR(
        id = "asr",
        displayName = "Asr",
        arabicName = "العصر",
        defaultTime = "16:15",
        rakats = 4,
        description = "Late afternoon prayer. 'Guard strictly your prayers, especially the middle prayer (Asr).'"
    ),
    MAGHRIB(
        id = "maghrib",
        displayName = "Maghrib",
        arabicName = "المغرب",
        defaultTime = "18:45",
        rakats = 3,
        description = "Dusk prayer offered immediately following sunset."
    ),
    ISHA(
        id = "isha",
        displayName = "Isha",
        arabicName = "العشاء",
        defaultTime = "20:15",
        rakats = 4,
        description = "Night prayer offered after the red twilight disappears completely from the sky."
    );

    val icon: ImageVector
        get() = when (this) {
            FAJR -> Icons.Default.WbTwilight
            DHUHR -> Icons.Default.Brightness7
            ASR -> Icons.Default.Brightness5
            MAGHRIB -> Icons.Default.Brightness2
            ISHA -> Icons.Default.NightsStay
        }

    companion object {
        fun fromId(id: String): PrayerType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FAJR
        }
    }
}
