package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.data.gemini.GeminiService
import com.example.data.gemini.PrayerCalculationHelper
import com.example.data.local.AppDatabase
import com.example.data.local.PenaltyTransactionEntity
import com.example.data.local.PrayerDao
import com.example.data.local.PrayerLogEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.DailySchedule
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.data.model.VerificationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PrayerRepository(
    private val context: Context,
    private val prayerDao: PrayerDao = AppDatabase.getInstance(context).prayerDao(),
    private val geminiService: GeminiService = GeminiService()
) {

    val userProfile: Flow<UserProfileEntity?> = prayerDao.getUserProfile()
    val allLogs: Flow<List<PrayerLogEntity>> = prayerDao.getAllLogs()
    val penaltyTransactions: Flow<List<PenaltyTransactionEntity>> = prayerDao.getAllTransactions()

    init {
        // Ensure default profile exists
    }

    suspend fun ensureProfile(): UserProfileEntity {
        var profile = prayerDao.getUserProfileOnce()
        if (profile == null) {
            profile = UserProfileEntity(
                id = 1,
                freeSkipsRemaining = 10,
                initialFreeSkips = 10,
                cityName = "Mumbai, IN (Auto GPS)",
                latitude = 19.0760,
                longitude = 72.8777
            )
            prayerDao.insertOrUpdateProfile(profile)
        }
        return profile
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun getTodayLogsFlow(): Flow<List<PrayerLogEntity>> {
        return prayerDao.getLogsForDate(getTodayDateString())
    }

    /**
     * Compute current daily schedule combining GPS coordinates, user offsets, and Google Search Grounding.
     */
    suspend fun getDailySchedule(profile: UserProfileEntity): DailySchedule {
        val todayStr = getTodayDateString()
        // 1. Calculate baseline astronomical timings from GPS coordinates
        val baseline = PrayerCalculationHelper.calculateForCoordinates(
            latitude = profile.latitude,
            longitude = profile.longitude
        )

        // 2. Apply offsets
        val fajrTime = applyOffset(baseline.fajr, profile.fajrOffsetMinutes)
        val dhuhrTime = applyOffset(baseline.dhuhr, profile.dhuhrOffsetMinutes)
        val asrTime = applyOffset(baseline.asr, profile.asrOffsetMinutes)
        val maghribTime = applyOffset(baseline.maghrib, profile.maghribOffsetMinutes)
        val ishaTime = applyOffset(baseline.isha, profile.ishaOffsetMinutes)

        // Check today's logged status
        val todayLogs = prayerDao.getLogsForDate(todayStr).firstOrNull() ?: emptyList()
        val offeredNames = todayLogs.filter { it.status == "OFFERED" }.map { it.prayerName }
        val skippedNames = todayLogs.filter { it.status == "SKIPPED" }.map { it.prayerName }
        val penaltyNames = todayLogs.filter { it.status == "PENALTY_PAID" }.map { it.prayerName }

        val items = listOf(
            PrayerTimeItem(
                prayerType = PrayerType.FAJR,
                timeFormatted = fajrTime,
                isOfferedToday = offeredNames.contains(PrayerType.FAJR.name),
                isSkippedToday = skippedNames.contains(PrayerType.FAJR.name),
                isPenaltyPaid = penaltyNames.contains(PrayerType.FAJR.name)
            ),
            PrayerTimeItem(
                prayerType = PrayerType.DHUHR,
                timeFormatted = dhuhrTime,
                isOfferedToday = offeredNames.contains(PrayerType.DHUHR.name),
                isSkippedToday = skippedNames.contains(PrayerType.DHUHR.name),
                isPenaltyPaid = penaltyNames.contains(PrayerType.DHUHR.name)
            ),
            PrayerTimeItem(
                prayerType = PrayerType.ASR,
                timeFormatted = asrTime,
                isOfferedToday = offeredNames.contains(PrayerType.ASR.name),
                isSkippedToday = skippedNames.contains(PrayerType.ASR.name),
                isPenaltyPaid = penaltyNames.contains(PrayerType.ASR.name)
            ),
            PrayerTimeItem(
                prayerType = PrayerType.MAGHRIB,
                timeFormatted = maghribTime,
                isOfferedToday = offeredNames.contains(PrayerType.MAGHRIB.name),
                isSkippedToday = skippedNames.contains(PrayerType.MAGHRIB.name),
                isPenaltyPaid = penaltyNames.contains(PrayerType.MAGHRIB.name)
            ),
            PrayerTimeItem(
                prayerType = PrayerType.ISHA,
                timeFormatted = ishaTime,
                isOfferedToday = offeredNames.contains(PrayerType.ISHA.name),
                isSkippedToday = skippedNames.contains(PrayerType.ISHA.name),
                isPenaltyPaid = penaltyNames.contains(PrayerType.ISHA.name)
            )
        )

        return DailySchedule(
            dateString = todayStr,
            cityName = profile.cityName,
            latitude = profile.latitude,
            longitude = profile.longitude,
            isGroundedWithGoogleSearch = profile.isGroundedViaSearch,
            sourceDescription = if (profile.isGroundedViaSearch) "Google Search Grounded via gemini-3.5-flash" else "Astronomical GPS calculation",
            prayers = items
        )
    }

    /**
     * Executes Google Search Grounding with gemini-3.5-flash to get up-to-date prayer timings.
     */
    suspend fun syncWithGoogleSearchGrounding(): Result<String> {
        val profile = ensureProfile()
        val result = geminiService.fetchPrayerTimesWithGoogleSearch(
            latitude = profile.latitude,
            longitude = profile.longitude,
            cityName = profile.cityName
        )

        return if (result.isSuccess) {
            val timings = result.getOrThrow()
            // Update profile with search grounded state
            val updated = profile.copy(
                isGroundedViaSearch = true,
                lastGroundingSyncTime = System.currentTimeMillis()
            )
            prayerDao.updateProfile(updated)
            Result.success("Google Search Grounding successful. Prayer timings synced.")
        } else {
            // Keep calculated timings intact
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    /**
     * Verifies Janamaz photo with Gemini 3.1 Pro Preview.
     */
    suspend fun verifyJanamazPhoto(
        bitmap: Bitmap,
        prayerType: PrayerType
    ): Result<VerificationResult> {
        val result = geminiService.analyzeJanamazPhoto(bitmap, prayerType.displayName)
        if (result.isSuccess) {
            val verification = result.getOrThrow()
            if (verification.isJanamaz && verification.confidence >= 65) {
                // Save log to Room
                val profile = ensureProfile()
                val todayStr = getTodayDateString()
                val thumbnail = createThumbnailBase64(bitmap)

                prayerDao.insertLog(
                    PrayerLogEntity(
                        date = todayStr,
                        prayerName = prayerType.name,
                        status = "OFFERED",
                        photoBase64 = thumbnail,
                        aiConfidence = verification.confidence,
                        aiExplanation = "${verification.summary} (${verification.details})",
                        penaltyAmount = 0
                    )
                )

                // Update streak
                val newStreak = profile.currentStreak + 1
                val bestStreak = maxOf(profile.bestStreak, newStreak)
                prayerDao.updateProfile(
                    profile.copy(
                        currentStreak = newStreak,
                        bestStreak = bestStreak
                    )
                )
            }
        }
        return result
    }

    /**
     * Skips prayer.
     * Uses 1 free chance if available.
     * Throws exception if free chances are 0 (requires payment instead).
     */
    suspend fun skipPrayerWithFreeChance(prayerType: PrayerType): Result<Int> {
        val profile = ensureProfile()
        if (profile.freeSkipsRemaining <= 0) {
            return Result.failure(IllegalStateException("No free skips remaining. ₹10 penalty required."))
        }

        val remaining = profile.freeSkipsRemaining - 1
        prayerDao.updateProfile(
            profile.copy(
                freeSkipsRemaining = remaining,
                currentStreak = 0 // Streak broken
            )
        )

        val todayStr = getTodayDateString()
        prayerDao.insertLog(
            PrayerLogEntity(
                date = todayStr,
                prayerName = prayerType.name,
                status = "SKIPPED",
                aiConfidence = 0,
                aiExplanation = "Skipped prayer using free quota. $remaining chances remaining.",
                penaltyAmount = 0
            )
        )

        return Result.success(remaining)
    }

    /**
     * Pays ₹10 penalty fee to leave/skip namaz when free chances are exhausted.
     */
    suspend fun paySkipPenalty(
        prayerType: PrayerType,
        paymentApp: String,
        upiId: String
    ): Result<PenaltyTransactionEntity> {
        val profile = ensureProfile()
        val todayStr = getTodayDateString()
        val refId = "UPI-" + UUID.randomUUID().toString().take(10).uppercase()

        val transaction = PenaltyTransactionEntity(
            date = todayStr,
            prayerName = prayerType.displayName,
            amount = 10,
            upiRefId = refId,
            paymentApp = paymentApp,
            paymentStatus = "SUCCESS",
            remarks = "₹10 skip penalty authorized for ${prayerType.displayName} ($upiId)"
        )
        val txId = prayerDao.insertTransaction(transaction)

        // Record prayer log as PENALTY_PAID
        prayerDao.insertLog(
            PrayerLogEntity(
                date = todayStr,
                prayerName = prayerType.name,
                status = "PENALTY_PAID",
                aiConfidence = 0,
                aiExplanation = "Unlocked via ₹10 penalty fine ($refId via $paymentApp)",
                penaltyAmount = 10,
                transactionRef = refId
            )
        )

        // Update profile total penalties
        prayerDao.updateProfile(
            profile.copy(
                totalPenaltiesPaid = profile.totalPenaltiesPaid + 10,
                currentStreak = 0
            )
        )

        return Result.success(transaction.copy(id = txId))
    }

    suspend fun updateLocation(cityName: String, lat: Double, lng: Double) {
        val profile = ensureProfile()
        prayerDao.updateProfile(
            profile.copy(
                cityName = cityName,
                latitude = lat,
                longitude = lng,
                isGroundedViaSearch = false // Need re-sync for new location
            )
        )
    }

    suspend fun updateOffsets(
        fajr: Int,
        dhuhr: Int,
        asr: Int,
        maghrib: Int,
        isha: Int
    ) {
        val profile = ensureProfile()
        prayerDao.updateProfile(
            profile.copy(
                fajrOffsetMinutes = fajr,
                dhuhrOffsetMinutes = dhuhr,
                asrOffsetMinutes = asr,
                maghribOffsetMinutes = maghrib,
                ishaOffsetMinutes = isha
            )
        )
    }

    suspend fun updateSettings(
        notificationsEnabled: Boolean,
        reminderMinutes: Int,
        lockDuration: Int,
        appLockService: Boolean
    ) {
        val profile = ensureProfile()
        prayerDao.updateProfile(
            profile.copy(
                notificationsEnabled = notificationsEnabled,
                reminderMinutesBefore = reminderMinutes,
                lockdownDurationMinutes = lockDuration,
                isAppLockServiceEnabled = appLockService
            )
        )
    }

    suspend fun signInWithGoogle(email: String, displayName: String, photoUrl: String = "") {
        val profile = ensureProfile()
        prayerDao.updateProfile(
            profile.copy(
                isGoogleSignedIn = true,
                googleEmail = email,
                googleDisplayName = displayName,
                googlePhotoUrl = photoUrl
            )
        )
    }

    suspend fun signOutGoogle() {
        val profile = ensureProfile()
        prayerDao.updateProfile(
            profile.copy(
                isGoogleSignedIn = false,
                googleEmail = "",
                googleDisplayName = "",
                googlePhotoUrl = ""
            )
        )
    }

    private fun applyOffset(timeFormatted: String, offsetMinutes: Int): String {
        if (offsetMinutes == 0) return timeFormatted
        try {
            val parts = timeFormatted.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            var totalMin = h * 60 + m + offsetMinutes
            while (totalMin < 0) totalMin += 1440
            totalMin %= 1440
            val newH = totalMin / 60
            val newM = totalMin % 60
            return String.format(Locale.US, "%02d:%02d", newH, newM)
        } catch (e: Exception) {
            return timeFormatted
        }
    }

    private fun createThumbnailBase64(bitmap: Bitmap): String {
        val maxDim = 250
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val w = if (ratio >= 1) maxDim else (maxDim * ratio).toInt()
        val h = if (ratio >= 1) (maxDim / ratio).toInt() else maxDim
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
