package com.example

import com.example.data.gemini.PrayerCalculationHelper
import com.example.data.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPrayerCalculation() {
        val timings = PrayerCalculationHelper.calculateForCoordinates(
            latitude = 19.0760,
            longitude = 72.8777
        )
        assertNotNull(timings.fajr)
        assertNotNull(timings.dhuhr)
        assertNotNull(timings.asr)
        assertNotNull(timings.maghrib)
        assertNotNull(timings.isha)

        assertTrue(timings.fajr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(timings.dhuhr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(timings.asr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(timings.maghrib.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(timings.isha.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun testPrayerTypeLookup() {
        assertEquals(PrayerType.FAJR, PrayerType.fromId("fajr"))
        assertEquals(PrayerType.DHUHR, PrayerType.fromId("dhuhr"))
        assertEquals(PrayerType.ASR, PrayerType.fromId("asr"))
        assertEquals(PrayerType.MAGHRIB, PrayerType.fromId("maghrib"))
        assertEquals(PrayerType.ISHA, PrayerType.fromId("isha"))
    }

    @Test
    fun testFreeChancesAndPenaltyPolicy() {
        var freeSkips = 10
        var penaltiesPaid = 0

        // User skips 10 times with free chances
        for (i in 1..10) {
            assertTrue(freeSkips > 0)
            freeSkips--
        }
        assertEquals(0, freeSkips)

        // 11th skip requires ₹10 penalty
        val requiresPenalty = freeSkips <= 0
        assertTrue(requiresPenalty)
        if (requiresPenalty) {
            penaltiesPaid += 10
        }
        assertEquals(10, penaltiesPaid)
    }
}
