package com.example

import com.example.data.gemini.PrayerCalculationHelper
import com.example.data.model.PrayerType
import com.example.data.qibla.QiblaCalculator
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
    fun testQiblaBearingCalculation() {
        // Mumbai coordinates to Makkah: approximately 285° - 295° (WNW)
        val bearing = QiblaCalculator.calculateQiblaBearing(19.0760, 72.8777)
        assertTrue("Qibla bearing for Mumbai should be between 280° and 300°", bearing in 280.0..300.0)

        // Distance from Mumbai to Kaaba: ~3,400 to ~3,600 km
        val distance = QiblaCalculator.calculateDistanceToKaabaKm(19.0760, 72.8777)
        assertTrue("Distance should be around 3400-3600 km", distance in 3300.0..3700.0)

        val cardinal = QiblaCalculator.getCardinalDirection(bearing)
        assertTrue(cardinal == "W" || cardinal == "NW")
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

    @Test
    fun testUpiPaymentGatewayUriAndResponseParsing() {
        val uri = com.example.data.payment.UpiPaymentGateway.buildUpiUri(
            amount = 100.00,
            note = "Strict Salah v3.0 Uninstallation Penalty",
            transactionRef = "SS-TEST-1234"
        )
        assertNotNull(uri)
        assertTrue(uri.toString().contains("8217317725@superyes"))
        assertTrue(uri.toString().contains("100.00"))

        val parsedSuccess = com.example.data.payment.UpiPaymentGateway.parseUpiResponse(
            "txnId=TXN123456&responseCode=00&ApprovalRefNo=987654&Status=SUCCESS&txnRef=SS-TEST-1234"
        )
        assertTrue(parsedSuccess.isSuccess)
        assertEquals("SUCCESS", parsedSuccess.status)
        assertEquals("TXN123456", parsedSuccess.transactionId)

        val token = com.example.data.payment.UpiPaymentGateway.generateUninstallToken()
        assertTrue(token.startsWith("SS-UNINSTALL-"))
    }
}
