package com.example.data.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaCalculator {

    // Holy Kaaba Coordinates in Makkah, Saudi Arabia
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the forward azimuth bearing (Qibla angle in degrees from True North, 0..360)
     * from the given GPS coordinates.
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLngRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLng = kaabaLngRad - userLngRad

        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLng)

        val initialBearingRad = atan2(y, x)
        val initialBearingDeg = Math.toDegrees(initialBearingRad)

        return (initialBearingDeg + 360.0) % 360.0
    }

    /**
     * Calculates the great-circle distance in kilometers from user coordinates to the Kaaba.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLngRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLat = kaabaLatRad - userLatRad
        val deltaLng = kaabaLngRad - userLngRad

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(userLatRad) * cos(kaabaLatRad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Returns standard cardinal abbreviation for a given angle in degrees (0..360)
     */
    fun getCardinalDirection(angleDeg: Double): String {
        val normalized = (angleDeg % 360.0 + 360.0) % 360.0
        return when {
            normalized >= 337.5 || normalized < 22.5 -> "N"
            normalized >= 22.5 && normalized < 67.5 -> "NE"
            normalized >= 67.5 && normalized < 112.5 -> "E"
            normalized >= 112.5 && normalized < 157.5 -> "SE"
            normalized >= 157.5 && normalized < 202.5 -> "S"
            normalized >= 202.5 && normalized < 247.5 -> "SW"
            normalized >= 247.5 && normalized < 292.5 -> "W"
            else -> "NW"
        }
    }
}
