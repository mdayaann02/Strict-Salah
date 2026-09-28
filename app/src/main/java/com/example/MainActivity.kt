package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PrayerType
import com.example.ui.components.FloatingLiquidGlassNavBar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.NavTabItem
import com.example.ui.components.UninstallPenaltyDialog
import com.example.ui.components.JanamazRegistrationDialog
import com.example.ui.components.ProfileDialog
import com.example.ui.components.PermissionsOnboardingDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockdownScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.LiquidAqua
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure window flags to allow drawing over lockscreen when lockdown is active
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Strict lockdown back press handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val state = viewModel.uiState.value
                if (state.isLockdownActive || state.currentScreen == AppScreen.LOCKDOWN) {
                    Toast.makeText(
                        this@MainActivity,
                        "🔒 Strict Lockdown Active! You cannot exit until Namaz is offered or skip fine is paid.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (state.currentScreen != AppScreen.HOME) {
                    viewModel.setScreen(AppScreen.HOME)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(
                themeMode = uiState.profile.themeMode,
                colorPalette = uiState.profile.colorPalette
            ) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If lockdown is active, keep app on screen
        if (viewModel.uiState.value.isLockdownActive) {
            viewModel.setScreen(AppScreen.LOCKDOWN)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // If user tries to press Home button while lockdown is active, immediately bring app back
        if (viewModel.uiState.value.isLockdownActive) {
            try {
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Handled
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun checkAllPermissionsGranted(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasLoc = fine || coarse
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        return hasLoc && hasNotif && hasCam && hasOverlay
    }

    var showPermissionsDialog by rememberSaveable { mutableStateOf(!checkAllPermissionsGranted()) }
    var hasAutoDetectedGpsOnStart by rememberSaveable { mutableStateOf(false) }

    // Auto-detect GPS if location permission is already available on start
    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if ((fine || coarse) && !hasAutoDetectedGpsOnStart) {
            hasAutoDetectedGpsOnStart = true
            viewModel.detectCurrentGpsLocation()
        }
    }

    // Seamless switch to Lockdown when prayer time lockdown triggers
    LaunchedEffect(uiState.isLockdownActive) {
        if (uiState.isLockdownActive) {
            viewModel.setScreen(AppScreen.LOCKDOWN)
        }
    }

    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    var hasAutoPromptedJanamaz by rememberSaveable { mutableStateOf(false) }

    // Auto-prompt Janamaz registration on first launch if not yet registered
    LaunchedEffect(uiState.profile.isJanamazRegistered) {
        if (!uiState.profile.isJanamazRegistered && !hasAutoPromptedJanamaz) {
            hasAutoPromptedJanamaz = true
            viewModel.openJanamazRegistration()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // If lockdown is explicitly active or screen is LOCKDOWN, render LockdownScreen full-screen
    if (uiState.isLockdownActive || uiState.currentScreen == AppScreen.LOCKDOWN) {
        LockdownScreen(
            uiState = uiState,
            onPhotoSelected = { viewModel.setCapturedPhoto(it) },
            onVerifyPhoto = { viewModel.verifyPhotoWithGemini() },
            onSkipRequested = { viewModel.onSkipPrayerRequested(it) },
            onDismissPayment = { viewModel.dismissPaymentSheet() },
            onConfirmPayment = { app, upiId -> viewModel.processPenaltyPayment(app, upiId) }
        )
        return
    }

    val isAmoled = uiState.profile.themeMode.equals("AMOLED", ignoreCase = true)

    val navItems = listOf(
        NavTabItem(
            screen = AppScreen.HOME,
            title = "Tracker",
            icon = Icons.Default.Home,
            testTag = "nav_home"
        ),
        NavTabItem(
            screen = AppScreen.QIBLA,
            title = "Qibla",
            icon = Icons.Default.Explore,
            testTag = "nav_qibla"
        ),
        NavTabItem(
            screen = AppScreen.STATISTICS,
            title = "Stats",
            icon = Icons.Default.BarChart,
            testTag = "nav_statistics"
        ),
        NavTabItem(
            screen = AppScreen.HISTORY,
            title = "History",
            icon = Icons.Default.History,
            testTag = "nav_history"
        ),
        NavTabItem(
            screen = AppScreen.SETTINGS,
            title = "Settings",
            icon = Icons.Default.Settings,
            testTag = "nav_settings"
        )
    )

    LiquidGlassBackground(isAmoled = isAmoled) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Allow liquid fluid background to shine through
            topBar = {
                // Frosted Liquid Glass Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = if (isAmoled) 0.95f else 0.85f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = if (isAmoled) 0.80f else 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (uiState.currentScreen) {
                                    AppScreen.HOME -> "Strict Salah 🕌"
                                    AppScreen.QIBLA -> "Qibla Compass 🧭"
                                    AppScreen.STATISTICS -> "Salah Statistics 📊"
                                    AppScreen.HISTORY -> "Salah Ledger"
                                    AppScreen.SETTINGS -> "Salah Schedule & Settings"
                                    else -> "Strict Salah"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { showProfileDialog = true },
                                modifier = Modifier.testTag("top_right_profile_button")
                            ) {
                                if (uiState.profile.isGoogleSignedIn && uiState.profile.googlePhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = uiState.profile.googlePhotoUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Profile & Account",
                                        tint = if (uiState.profile.isGoogleSignedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            bottomBar = {
                // Floating Translucent Liquid Glass Navigation Bar with Dynamic Water Droplet Animation
                FloatingLiquidGlassNavBar(
                    currentScreen = uiState.currentScreen,
                    items = navItems,
                    onTabSelected = { viewModel.setScreen(it) },
                    isAmoled = isAmoled
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        uiState = uiState,
                        onOpenActiveLock = { prayer -> viewModel.setScreen(AppScreen.LOCKDOWN) },
                        onPrayerClick = { prayer -> /* information */ },
                        onOpenQibla = { viewModel.setScreen(AppScreen.QIBLA) },
                        onSyncSearchGrounding = { viewModel.triggerGoogleSearchSync() },
                        onUpdateLocation = { city, lat, lng -> viewModel.updateLocation(city, lat, lng) }
                    )
                    AppScreen.QIBLA -> QiblaScreen(
                        uiState = uiState,
                        onRefreshLocation = { viewModel.detectCurrentGpsLocation() }
                    )
                    AppScreen.STATISTICS -> StatisticsScreen(
                        uiState = uiState,
                        onSignInGoogle = { email, name -> viewModel.signInWithGoogle(email, name) },
                        onSignOutGoogle = { viewModel.signOutGoogle() },
                        onBackupToDrive = { viewModel.syncDataToGoogleDrive() }
                    )
                    AppScreen.HISTORY -> HistoryScreen(uiState = uiState)
                    AppScreen.SETTINGS -> SettingsScreen(
                        uiState = uiState,
                        onSaveOffsets = { fajr, dhuhr, asr, maghrib, isha ->
                            viewModel.updateOffsets(fajr, dhuhr, asr, maghrib, isha)
                        },
                        onSaveSettings = { notif, rem, lock, appLock ->
                            viewModel.updateSettings(notif, rem, lock, appLock)
                        },
                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                        onSetColorPalette = { palette -> viewModel.setColorPalette(palette) },
                        onTriggerSearchSync = { viewModel.triggerGoogleSearchSync() },
                        onOpenJanamazRegistration = { viewModel.openJanamazRegistration() },
                        onOpenDeRegistrationPledge = { viewModel.openDeRegistrationDialog() },
                        onUpdatePrayerTiming = { prayer, time24 -> viewModel.updateCustomPrayerTiming(prayer, time24) },
                        onResetPrayerTiming = { prayer ->
                            viewModel.updateCustomPrayerTiming(prayer, "")
                        },
                        onResetAllPrayerTimings = { viewModel.resetPrayerTimingsToDefault() },
                        onToggleUseCustomTimings = { enabled -> viewModel.toggleUseCustomTimings(enabled) }
                    )
                    AppScreen.LOCKDOWN -> {
                        // Handled above
                    }
                }

                if (showPermissionsDialog) {
                    PermissionsOnboardingDialog(
                        onAllPermissionsGranted = {
                            showPermissionsDialog = false
                            viewModel.detectCurrentGpsLocation()
                        },
                        onDismiss = {
                            showPermissionsDialog = false
                            viewModel.detectCurrentGpsLocation()
                        }
                    )
                }

                if (showProfileDialog) {
                    ProfileDialog(
                        profile = uiState.profile,
                        onSignInGoogle = { email, name, photoUrl -> viewModel.signInWithGoogle(email, name, photoUrl) },
                        onSignOutGoogle = { viewModel.signOutGoogle() },
                        onBackupToDrive = { viewModel.syncDataToGoogleDrive() },
                        isSyncingDrive = uiState.isSyncingDrive,
                        lastDriveBackupTime = uiState.lastDriveBackupTime,
                        onDismiss = { showProfileDialog = false }
                    )
                }

                if (uiState.showJanamazRegistrationDialog) {
                    JanamazRegistrationDialog(
                        existingBitmaps = uiState.registeredJanamazBitmaps,
                        onSaveJanamazPhotos = { bitmaps -> viewModel.registerJanamazPhotos(bitmaps) },
                        onDismiss = { viewModel.dismissJanamazRegistration() },
                        isSaving = uiState.isRegisteringJanamaz
                    )
                }

                if (uiState.showUninstallPenaltyDialog || uiState.showDeRegistrationDialog) {
                    UninstallPenaltyDialog(
                        isUninstallUnlocked = uiState.profile.isUninstallUnlocked && (System.currentTimeMillis() < uiState.profile.uninstallUnlockExpiry || uiState.profile.uninstallUnlockExpiry == 0L && uiState.profile.isUninstallUnlocked),
                        unlockToken = uiState.profile.uninstallUnlockToken,
                        unlockExpiry = uiState.profile.uninstallUnlockExpiry,
                        isProcessing = uiState.isAnalyzing,
                        onDismiss = { viewModel.dismissUninstallPenaltyDialog() },
                        onConfirmUninstallPayment = { app, upiRef ->
                            viewModel.processUninstallPenaltyPayment(app, upiRef)
                        }
                    )
                }
            }
        }
    }
}
