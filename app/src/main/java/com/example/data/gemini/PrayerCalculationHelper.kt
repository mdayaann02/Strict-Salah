package com.example.data.gemini

import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Standard astronomical Islamic prayer calculation based on solar declination and equation of time.
 * Provides instant fallback timings based on GPS coordinates.
 */
object PrayerCalculationHelper {

    data class CalculatedTimings(
        val fajr: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    fun calculateForCoordinates(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        timeZoneOffsetHours: Double = (java.util.TimeZone.getDefault().rawOffset + java.util.TimeZone.getDefault().dstSavings) / 3600000.0
    ): CalculatedTimings {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        // Fractional year in radians
        val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1)

        // Equation of time in minutes
        val eqtime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        // Solar declination in radians
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) - 0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

        // Solar noon in hours UTC
        val latRad = Math.toRadians(latitude)
        val solarNoonUtc = (720.0 - 4.0 * longitude - eqtime) / 60.0
        val solarNoonLocal = solarNoonUtc + timeZoneOffsetHours

        // Dhuhr: solar noon + ~2 min safety
        val dhuhrHours = solarNoonLocal + 4.0 / 60.0

        // Fajr (18 degrees below horizon)
        val fajrAngle = Math.toRadians(18.0)
        val fajrHourAngle = computeHourAngle(latRad, decl, -fajrAngle)
        val fajrHours = solarNoonLocal - (fajrHourAngle / 15.0)

        // Maghrib (sunset, 0.833 degrees refraction)
        val sunsetAngle = Math.toRadians(-0.833)
        val sunsetHourAngle = computeHourAngle(latRad, decl, sunsetAngle)
        val maghribHours = solarNoonLocal + (sunsetHourAngle / 15.0)

        // Asr (Shafi'i shadow ratio = 1)
        val asrAltitude = atan(1.0 / (1.0 + tan(Math.abs(latRad - decl))))
        val asrHourAngle = computeHourAngle(latRad, decl, asrAltitude)
        val asrHours = solarNoonLocal + (asrHourAngle / 15.0)

        // Isha (18 degrees below horizon)
        val ishaAngle = Math.toRadians(18.0)
        val ishaHourAngle = computeHourAngle(latRad, decl, -ishaAngle)
        val ishaHours = solarNoonLocal + (ishaHourAngle / 15.0)

        return CalculatedTimings(
            fajr = formatHours(fajrHours),
            dhuhr = formatHours(dhuhrHours),
            asr = formatHours(asrHours),
            maghrib = formatHours(maghribHours),
            isha = formatHours(ishaHours)
        )
    }

    private fun computeHourAngle(lat: Double, decl: Double, altitude: Double): Double {
        val cosHA = (sin(altitude) - sin(lat) * sin(decl)) / (cos(lat) * cos(decl))
        val clamped = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    private fun formatHours(hoursDecimal: Double): String {
        var h = hoursDecimal
        while (h < 0) h += 24.0
        while (h >= 24) h -= 24.0

        val hours = floor(h).toInt()
        val minutes = ((h - hours) * 60.0).toInt().coerceIn(0, 59)
        return String.format(Locale.US, "%02d:%02d", hours, minutes)
    }
}
