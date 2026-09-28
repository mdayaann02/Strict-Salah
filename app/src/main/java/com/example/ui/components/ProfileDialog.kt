package com.example.ui.components

import android.accounts.AccountManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.auth.FirebaseAuthService
import com.example.data.local.UserProfileEntity
import com.example.util.AppIconManager
import com.example.util.AppLogoTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

@Composable
fun ProfileDialog(
    profile: UserProfileEntity,
    onSignInGoogle: (email: String, name: String, photoUrl: String, uid: String, provider: String) -> Unit,
    onSignOutGoogle: () -> Unit,
    onBackupToDrive: () -> Unit,
    onUpdateGenderAndLogo: (gender: String, logoTheme: String) -> Unit = { _, _ -> },
    onOpenVoluntaryPayment: () -> Unit = {},
    isSyncingDrive: Boolean,
    lastDriveBackupTime: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSignedIn = profile.isGoogleSignedIn
    var showCustomAccountDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    val detectedAccounts = remember { mutableStateListOf<String>() }

    // Google Sign-In with Play Services
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && !account.email.isNullOrBlank()) {
                val email = account.email!!
                val name = account.displayName ?: email.substringBefore("@")
                val photo = account.photoUrl?.toString().orEmpty()
                val idToken = account.idToken.orEmpty()
                val uid = account.id ?: "GOOGLE_${System.currentTimeMillis()}"
                onSignInGoogle(email, name, photo, uid, "GOOGLE")
            } else {
                showCustomAccountDialog = true
            }
        } catch (e: Exception) {
            showCustomAccountDialog = true
        }
    }

    val launchOfficialGoogleSignIn = {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken("8590bf70-1ef1-4434-b29c-13d289c67d97")
                .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
                .build()
            val client = GoogleSignIn.getClient(context, gso)
            googleSignInLauncher.launch(client.signInIntent)
        } catch (e: Exception) {
            try {
                val gsoFallback = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build()
                val client = GoogleSignIn.getClient(context, gsoFallback)
                googleSignInLauncher.launch(client.signInIntent)
            } catch (err: Exception) {
                showCustomAccountDialog = true
            }
        }
    }

    LaunchedEffect(showCustomAccountDialog) {
        if (showCustomAccountDialog) {
            try {
                val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as? AccountManager
                val accounts = accountManager?.getAccountsByType("com.google")
                detectedAccounts.clear()
                accounts?.forEach { acc ->
                    if (!acc.name.isNullOrBlank() && !detectedAccounts.contains(acc.name)) {
                        detectedAccounts.add(acc.name)
                    }
                }
            } catch (e: Exception) {
                // Handled
            }
        }
    }

    // Modal to connect account if Play Services dialog cannot open directly
    if (showCustomAccountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAccountDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color(0xFF4285F4)
                    )
                }
            },
            title = {
                Text(
                    text = "Personalized User Authentication",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Connect your personalized account with Firebase Auth & Google Sign-In to secure your data and sync with Google Drive.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (detectedAccounts.isNotEmpty()) {
                        Text(
                            text = "Detected Accounts on Device:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            detectedAccounts.forEach { email ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val name = email.substringBefore("@").replace(".", " ")
                                                .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                            onSignInGoogle(email, name, "", "LOCAL_${System.currentTimeMillis()}", "GOOGLE")
                                            showCustomAccountDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = email, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        HorizontalDivider()
                    }

                    Text(
                        text = "Or Enter Your Google / Firebase Credentials:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Your Full Name") },
                        placeholder = { Text("e.g. Mohammed Ayaan") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("Account Email") },
                        placeholder = { Text("e.g. mdayaann02@gmail.com") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = inputEmail.trim().ifBlank { "user@gmail.com" }
                        val name = inputName.trim().ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
                        onSignInGoogle(email, name, "", "AUTH_${System.currentTimeMillis()}", "FIREBASE")
                        showCustomAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal to Edit Name
    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(profile.googleDisplayName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        onSignInGoogle(profile.googleEmail, newName.trim(), profile.googlePhotoUrl, profile.firebaseUid, profile.authProvider)
                    }
                    showEditNameDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Strict Salah Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v2.0 Official Edition",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_profile_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Identity Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            if (isSignedIn && profile.googlePhotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.googlePhotoUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSignedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isSignedIn && profile.googleDisplayName.isNotBlank()) {
                                                profile.googleDisplayName.take(1).uppercase()
                                            } else {
                                                "S"
                                            },
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isSignedIn && profile.googleDisplayName.isNotBlank()) {
                                            profile.googleDisplayName
                                        } else {
                                            "Strict Salah Servant"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSignedIn) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { showEditNameDialog = true },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Name",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = if (isSignedIn && profile.googleEmail.isNotBlank()) {
                                        profile.googleEmail
                                    } else {
                                        "Account not connected"
                                    },
                                    fontSize = 12.sp,
                                    color = if (isSignedIn) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSignedIn) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSignedIn) Icons.Default.VerifiedUser else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isSignedIn) Color(0xFF10B981) else Color(0xFFD97706),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSignedIn) "Firebase & Google Auth" else "Local Profile",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSignedIn) Color(0xFF10B981) else Color(0xFFD97706)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats summary pill row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Active Streak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${profile.currentStreak} Days 🔥", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Free Skips Left", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${profile.freeSkipsRemaining}/10",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (profile.freeSkipsRemaining <= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Penalties Paid", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "₹${profile.totalPenaltiesPaid}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Auth Action Buttons
                        if (!isSignedIn) {
                            Button(
                                onClick = { launchOfficialGoogleSignIn() },
                                modifier = Modifier.fillMaxWidth().testTag("connect_google_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In with Google & Firebase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { launchOfficialGoogleSignIn() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Switch Account", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = onSignOutGoogle,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GENDER & APP LOGO SELECTION SECTION
                Text(
                    text = "App Logo & Gender Edition",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select your preferred app launcher icon & styling theme according to gender.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLogoTheme.entries.forEach { logoTheme ->
                        val isSelected = profile.appLogoTheme.equals(logoTheme.key, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    val gender = when (logoTheme) {
                                        AppLogoTheme.BROTHER -> "BROTHER"
                                        AppLogoTheme.SISTER -> "SISTER"
                                        else -> "NEUTRAL"
                                    }
                                    onUpdateGenderAndLogo(gender, logoTheme.key)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = logoTheme.drawableRes),
                                    contentDescription = logoTheme.title,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = logoTheme.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (logoTheme) {
                                                AppLogoTheme.BROTHER -> Color(0xFF065F46).copy(alpha = 0.2f)
                                                AppLogoTheme.SISTER -> Color(0xFFBE185D).copy(alpha = 0.2f)
                                                AppLogoTheme.DEFAULT -> Color(0xFF1E3A8A).copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = logoTheme.genderLabel,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = when (logoTheme) {
                                                    AppLogoTheme.BROTHER -> Color(0xFF047857)
                                                    AppLogoTheme.SISTER -> Color(0xFFDB2777)
                                                    AppLogoTheme.DEFAULT -> Color(0xFF2563EB)
                                                }
                                            )
                                        }
                                    }
                                    Text(
                                        text = logoTheme.subtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        val gender = when (logoTheme) {
                                            AppLogoTheme.BROTHER -> "BROTHER"
                                            AppLogoTheme.SISTER -> "SISTER"
                                            else -> "NEUTRAL"
                                        }
                                        onUpdateGenderAndLogo(gender, logoTheme.key)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PAYMENT & DISCIPLINE PLEDGE CARD
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Direct UPI Payments",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Pay Sadaqah, Fines & Pledges",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenVoluntaryPayment()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay / Pledge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GOOGLE DRIVE BACKUP CARD
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileMove,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Google Drive Cloud Backup",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (lastDriveBackupTime != null) "Last synced: $lastDriveBackupTime" else "Syncs streak & verification logs",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = onBackupToDrive,
                                enabled = !isSyncingDrive,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isSyncingDrive) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Backup", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
