package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PrayerType
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockdownScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle intent from notifications
        intent?.let { handleIntent(it) }

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        if (intent.getBooleanExtra("TRIGGER_LOCKDOWN", false)) {
            val prayerId = intent.getStringExtra("PRAYER_TYPE") ?: "fajr"
            viewModel.startLockdown(PrayerType.fromId(prayerId))
        } else if (intent.hasExtra("OPEN_PRAYER")) {
            val prayerId = intent.getStringExtra("OPEN_PRAYER") ?: "fajr"
            viewModel.startLockdown(PrayerType.fromId(prayerId))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Sudden auto-open when prayer time lockdown triggers
    LaunchedEffect(uiState.isLockdownActive) {
        if (uiState.isLockdownActive) {
            viewModel.setScreen(AppScreen.LOCKDOWN)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                try {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handled
                }
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // If lockdown is explicitly active and screen is LOCKDOWN, render LockdownScreen full-screen
    if (uiState.currentScreen == AppScreen.LOCKDOWN) {
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.currentScreen) {
                            AppScreen.HOME -> "Strict Namaz 🕌"
                            AppScreen.STATISTICS -> "Salah Statistics & Progress 📊"
                            AppScreen.HISTORY -> "Prayer & Penalty Ledger"
                            AppScreen.SETTINGS -> "Prayer Schedule & Settings"
                            else -> "Strict Namaz"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.HOME,
                    onClick = { viewModel.setScreen(AppScreen.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Tracker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.STATISTICS,
                    onClick = { viewModel.setScreen(AppScreen.STATISTICS) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
                    label = { Text("Statistics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_statistics")
                )

                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.HISTORY,
                    onClick = { viewModel.setScreen(AppScreen.HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_history")
                )

                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.setScreen(AppScreen.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
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
                    onSyncSearchGrounding = { viewModel.triggerGoogleSearchSync() },
                    onUpdateLocation = { city, lat, lng -> viewModel.updateLocation(city, lat, lng) },
                    onSignInGoogle = { email, name -> viewModel.signInWithGoogle(email, name) },
                    onSignOutGoogle = { viewModel.signOutGoogle() }
                )
                AppScreen.STATISTICS -> StatisticsScreen(
                    uiState = uiState,
                    onSignInGoogle = { email, name -> viewModel.signInWithGoogle(email, name) },
                    onSignOutGoogle = { viewModel.signOutGoogle() }
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
                    onTriggerSearchSync = { viewModel.triggerGoogleSearchSync() }
                )
                AppScreen.LOCKDOWN -> {
                    // Handled above
                }
            }
        }
    }
}
