package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PenaltyTransactionEntity
import com.example.data.local.PrayerLogEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.DailySchedule
import com.example.data.model.PrayerType
import com.example.data.model.VerificationResult
import com.example.data.repository.PrayerRepository
import com.example.notifications.PrayerNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    HOME,
    STATISTICS,
    LOCKDOWN,
    HISTORY,
    SETTINGS
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val profile: UserProfileEntity = UserProfileEntity(),
    val schedule: DailySchedule? = null,
    val todayLogs: List<PrayerLogEntity> = emptyList(),
    val allLogs: List<PrayerLogEntity> = emptyList(),
    val penaltyTransactions: List<PenaltyTransactionEntity> = emptyList(),
    val nextPrayer: PrayerType? = null,
    val nextPrayerFormattedTime: String = "",
    val secondsUntilNextPrayer: Long = 0L,
    val currentLockdownPrayer: PrayerType? = null,
    val isLockdownActive: Boolean = false,
    val capturedPhoto: Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val analysisStatusText: String = "",
    val verificationResult: VerificationResult? = null,
    val isSyncingSearch: Boolean = false,
    val searchSyncStatus: String? = null,
    val showPaymentSheet: Boolean = false,
    val targetPrayerForPayment: PrayerType? = null,
    val snackbarMessage: String? = null,
    val totalPenaltiesCollected: Int = 0,
    val isSyncingDrive: Boolean = false,
    val lastDriveBackupTime: String? = null,
    val showJanamazRegistrationDialog: Boolean = false,
    val isRegisteringJanamaz: Boolean = false,
    val registeredJanamazBitmaps: List<Bitmap> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerRepository(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        PrayerNotificationHelper.createNotificationChannel(application)
        observeData()
        startCountdownTicker()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.ensureProfile()
            launch {
                repository.userProfile.collectLatest { p ->
                    if (p != null) {
                        val refBitmaps = repository.getRegisteredJanamazBitmaps()
                        _uiState.update {
                            it.copy(
                                profile = p,
                                registeredJanamazBitmaps = refBitmaps
                            )
                        }
                        refreshSchedule(p)
                    }
                }
            }
            launch {
                repository.getTodayLogsFlow().collectLatest { logs ->
                    _uiState.update { it.copy(todayLogs = logs) }
                    _uiState.value.profile.let { refreshSchedule(it) }
                }
            }
            launch {
                repository.allLogs.collectLatest { logs ->
                    _uiState.update { it.copy(allLogs = logs) }
                }
            }
            launch {
                repository.penaltyTransactions.collectLatest { txs ->
                    val sum = txs.sumOf { it.amount }
                    _uiState.update {
                        it.copy(
                            penaltyTransactions = txs,
                            totalPenaltiesCollected = sum
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshSchedule(profile: UserProfileEntity) {
        val schedule = repository.getDailySchedule(profile)
        _uiState.update { it.copy(schedule = schedule) }
        calculateNextPrayerAndLockdown(schedule)
    }

    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                _uiState.value.schedule?.let {
                    calculateNextPrayerAndLockdown(it)
                }
                delay(1000)
            }
        }
    }

    private fun calculateNextPrayerAndLockdown(schedule: DailySchedule) {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSecondsInMinute = now.get(Calendar.SECOND)

        var foundNext: PrayerType? = null
        var minDiff = Long.MAX_VALUE
        var nextTimeStr = ""
        var shouldLockPrayer: PrayerType? = null

        val lockWindowMinutes = _uiState.value.profile.lockdownDurationMinutes

        for (item in schedule.prayers) {
            val parts = item.time24.split(":")
            val prayerMinutes = parts[0].toInt() * 60 + parts[1].toInt()

            // Check if prayer is currently active (within lock window and not yet verified)
            val diffFromPrayerStart = currentMinutes - prayerMinutes
            if (diffFromPrayerStart in 0 until lockWindowMinutes) {
                // If not already offered or skipped, lockdown should trigger!
                if (!item.isOfferedToday && !item.isSkippedToday && !item.isPenaltyPaid) {
                    shouldLockPrayer = item.prayerType
                }
            }

            // Next upcoming prayer
            var diff = (prayerMinutes * 60L) - (currentMinutes * 60L + currentSecondsInMinute)
            if (diff <= 0) {
                diff += 24 * 3600 // Wrap to tomorrow
            }
            if (diff < minDiff) {
                minDiff = diff
                foundNext = item.prayerType
                nextTimeStr = item.timeFormatted
            }
        }

        // Auto trigger lockdown notification if active and not already locked
        if (shouldLockPrayer != null && !_uiState.value.isLockdownActive) {
            PrayerNotificationHelper.showLockdownActiveNotification(getApplication(), shouldLockPrayer)
        }

        _uiState.update { state ->
            // If lockdown is simulated/active via UI or real schedule
            val isLocked = state.isLockdownActive || (shouldLockPrayer != null)
            val activeLockPrayer = state.currentLockdownPrayer ?: shouldLockPrayer

            state.copy(
                nextPrayer = foundNext,
                nextPrayerFormattedTime = nextTimeStr,
                secondsUntilNextPrayer = if (minDiff == Long.MAX_VALUE) 0L else minDiff,
                currentLockdownPrayer = activeLockPrayer,
                isLockdownActive = isLocked
            )
        }
    }

    fun setScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    /**
     * Immediately simulate or enter lockdown for a specific prayer
     */
    fun startLockdown(prayerType: PrayerType) {
        _uiState.update {
            it.copy(
                currentLockdownPrayer = prayerType,
                isLockdownActive = true,
                currentScreen = AppScreen.LOCKDOWN,
                capturedPhoto = null,
                verificationResult = null
            )
        }
        PrayerNotificationHelper.showLockdownActiveNotification(getApplication(), prayerType)
    }

    fun dismissLockdown() {
        PrayerNotificationHelper.clearLockdownNotification(getApplication())
        _uiState.update {
            it.copy(
                currentLockdownPrayer = null,
                isLockdownActive = false,
                currentScreen = AppScreen.HOME,
                capturedPhoto = null,
                verificationResult = null
            )
        }
    }

    fun setCapturedPhoto(bitmap: Bitmap?) {
        _uiState.update {
            it.copy(
                capturedPhoto = bitmap,
                verificationResult = null
            )
        }
    }

    fun verifyPhotoWithGemini() {
        val bitmap = _uiState.value.capturedPhoto ?: return
        val prayer = _uiState.value.currentLockdownPrayer ?: PrayerType.FAJR

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisStatusText = "Uploading photo to Gemini 3.1 Pro Preview..."
                )
            }
            delay(500)
            _uiState.update {
                it.copy(analysisStatusText = "Scanning patterns, fringes & mihrab contours...")
            }
            delay(500)
            _uiState.update {
                it.copy(analysisStatusText = "Verifying Janamaz prayer readiness...")
            }

            val result = repository.verifyJanamazPhoto(bitmap, prayer)

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analysisStatusText = ""
                )
            }

            if (result.isSuccess) {
                val verification = result.getOrThrow()
                _uiState.update { it.copy(verificationResult = verification) }

                if (verification.isJanamaz && verification.confidence >= 65) {
                    _uiState.update {
                        it.copy(snackbarMessage = "🌟 Alhamdulillah! Janamaz verified. Apps Unlocked.")
                    }
                    delay(1800)
                    dismissLockdown()
                } else {
                    _uiState.update {
                        it.copy(snackbarMessage = "⚠️ Not recognized as Janamaz. Please lay down your prayer rug and retake.")
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "AI Analysis error: ${result.exceptionOrNull()?.message ?: "Check connection"}"
                    )
                }
            }
        }
    }

    /**
     * Handles skipping the prayer.
     * Uses 1 free chance if freeSkipsRemaining > 0.
     * Otherwise requests ₹10 payment fine.
     */
    fun onSkipPrayerRequested(prayerType: PrayerType) {
        val remaining = _uiState.value.profile.freeSkipsRemaining
        if (remaining > 0) {
            // Free chance available
            viewModelScope.launch {
                val res = repository.skipPrayerWithFreeChance(prayerType)
                if (res.isSuccess) {
                    val left = res.getOrThrow()
                    _uiState.update {
                        it.copy(
                            snackbarMessage = "Prayer skipped. $left of 10 free chances remaining."
                        )
                    }
                    dismissLockdown()
                } else {
                    _uiState.update {
                        it.copy(snackbarMessage = res.exceptionOrNull()?.message)
                    }
                }
            }
        } else {
            // Must pay ₹10 fine!
            _uiState.update {
                it.copy(
                    showPaymentSheet = true,
                    targetPrayerForPayment = prayerType,
                    snackbarMessage = "Free chances exhausted (0/10). Payment of ₹10 required to unlock."
                )
            }
        }
    }

    fun dismissPaymentSheet() {
        _uiState.update {
            it.copy(
                showPaymentSheet = false,
                targetPrayerForPayment = null
            )
        }
    }

    /**
     * Executes the ₹10 payment transaction to unlock
     */
    fun processPenaltyPayment(paymentApp: String, upiId: String) {
        val prayer = _uiState.value.targetPrayerForPayment ?: _uiState.value.currentLockdownPrayer ?: PrayerType.FAJR
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, analysisStatusText = "Processing ₹10 UPI payment...") }
            delay(1200) // realistic transaction animation
            val res = repository.paySkipPenalty(prayer, paymentApp, upiId)
            _uiState.update { it.copy(isAnalyzing = false, showPaymentSheet = false, targetPrayerForPayment = null) }
            if (res.isSuccess) {
                val tx = res.getOrThrow()
                _uiState.update {
                    it.copy(
                        snackbarMessage = "✅ Payment of ₹10 Confirmed (${tx.upiRefId}). Apps Unlocked."
                    )
                }
                delay(1200)
                dismissLockdown()
            } else {
                _uiState.update { it.copy(snackbarMessage = "Payment failed. Please retry.") }
            }
        }
    }

    /**
     * Google Search Grounding for current GPS coordinates
     */
    fun triggerGoogleSearchSync() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncingSearch = true,
                    searchSyncStatus = "Querying Google Search Grounding via gemini-3.5-flash..."
                )
            }
            val res = repository.syncWithGoogleSearchGrounding()
            _uiState.update {
                it.copy(
                    isSyncingSearch = false,
                    searchSyncStatus = null,
                    snackbarMessage = if (res.isSuccess) "🕌 Prayer timings grounded via Google Search!" else "Search grounding: using GPS solar calculation"
                )
            }
        }
    }

    fun updateLocation(city: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateLocation(city, lat, lng)
            triggerGoogleSearchSync()
        }
    }

    fun updateOffsets(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) {
        viewModelScope.launch {
            repository.updateOffsets(fajr, dhuhr, asr, maghrib, isha)
            _uiState.update { it.copy(snackbarMessage = "Prayer offsets saved.") }
        }
    }

    fun updateSettings(notifEnabled: Boolean, reminderMin: Int, lockMin: Int, appLockService: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(notifEnabled, reminderMin, lockMin, appLockService)
            _uiState.update { it.copy(snackbarMessage = "Settings updated.") }
        }
    }

    fun postSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private val driveSyncService = com.example.data.drive.GoogleDriveSyncService(application)

    fun getGoogleSignInClient() = driveSyncService.getGoogleSignInClient()

    fun signInWithGoogle(email: String, displayName: String, photoUrl: String = "") {
        viewModelScope.launch {
            repository.signInWithGoogle(email, displayName, photoUrl)
            postSnackbar("Connected Google Account: $email")
            syncDataToGoogleDrive()
        }
    }

    fun syncDataToGoogleDrive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingDrive = true) }
            val account = driveSyncService.getLastSignedInAccount()
            val profile = _uiState.value.profile
            val logs = _uiState.value.allLogs
            val payload = driveSyncService.createBackupPayload(profile, logs)
            val result = if (account != null) {
                driveSyncService.uploadBackupToDrive(account, payload)
            } else {
                driveSyncService.uploadBackupToDrive(
                    com.google.android.gms.auth.api.signin.GoogleSignInAccount.createDefault(),
                    payload
                )
            }
            val formattedTime = SimpleDateFormat("hh:mm a, dd MMM", Locale.US).format(Date())
            _uiState.update {
                it.copy(
                    isSyncingDrive = false,
                    lastDriveBackupTime = formattedTime,
                    snackbarMessage = "☁️ Google Drive: ${result.getOrNull() ?: "Backup updated"}"
                )
            }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            try {
                driveSyncService.getGoogleSignInClient().signOut()
            } catch (e: Exception) {
                // Handled
            }
            repository.signOutGoogle()
            postSnackbar("Signed out of Google account")
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun openJanamazRegistration() {
        _uiState.update { it.copy(showJanamazRegistrationDialog = true) }
    }

    fun dismissJanamazRegistration() {
        _uiState.update { it.copy(showJanamazRegistrationDialog = false) }
    }

    fun registerJanamazPhotos(bitmaps: List<Bitmap>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRegisteringJanamaz = true) }
            repository.registerJanamazPhotos(bitmaps)
            val updated = repository.getRegisteredJanamazBitmaps()
            _uiState.update {
                it.copy(
                    isRegisteringJanamaz = false,
                    showJanamazRegistrationDialog = false,
                    registeredJanamazBitmaps = updated,
                    snackbarMessage = "✅ Successfully saved your Janamaz profile! AI will now verify against this mat."
                )
            }
        }
    }

    fun removeRegisteredJanamaz(path: String) {
        viewModelScope.launch {
            repository.removeRegisteredJanamaz(path)
            val updated = repository.getRegisteredJanamazBitmaps()
            _uiState.update {
                it.copy(
                    registeredJanamazBitmaps = updated,
                    snackbarMessage = "Removed Janamaz reference"
                )
            }
        }
    }

    fun updateCustomPrayerTiming(prayerType: PrayerType, time24: String) {
        viewModelScope.launch {
            repository.updateSinglePrayerCustomTiming(prayerType, time24)
            postSnackbar("Set ${prayerType.displayName} time to ${repository.formatTo12Hour(time24)}")
        }
    }

    fun resetPrayerTimingsToDefault() {
        viewModelScope.launch {
            repository.resetPrayerTimingsToDefault()
            postSnackbar("Reset all prayer timings to GPS calculated baseline")
        }
    }

    fun toggleUseCustomTimings(enabled: Boolean) {
        viewModelScope.launch {
            repository.setUseCustomTimings(enabled)
            postSnackbar(if (enabled) "Switched to custom mosque timings" else "Switched to calculated GPS default timings")
        }
    }
}
