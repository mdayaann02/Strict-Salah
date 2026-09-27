package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.ui.components.PaymentDialog
import com.example.ui.viewmodel.MainUiState

@Composable
fun LockdownScreen(
    uiState: MainUiState,
    onPhotoSelected: (Bitmap?) -> Unit,
    onVerifyPhoto: () -> Unit,
    onSkipRequested: (PrayerType) -> Unit,
    onDismissPayment: () -> Unit,
    onConfirmPayment: (app: String, upiId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prayer = uiState.currentLockdownPrayer ?: PrayerType.FAJR
    val freeSkips = uiState.profile.freeSkipsRemaining
    var showSkipConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back navigation to maintain active lockdown mode until authorized
    BackHandler(enabled = uiState.isLockdownActive) {
        // Can only exit via verification or skip/payment flow
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onPhotoSelected(bitmap)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onPhotoSelected(bitmap)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // Pulse animation for lockdown banner
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("lockdown_screen"),
        color = Color(0xFF07120D)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Urgency Alert Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD32F2F).copy(alpha = alphaAnim * 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NAMAZ TIME HAS STARTED",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "All phone apps are locked until prayer is verified",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Prayer Hero Header
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5C158).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = prayer.icon,
                    contentDescription = prayer.displayName,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${prayer.displayName} Prayer (${prayer.arabicName})",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "${prayer.rakats} Rak'ahs • Time to pray and connect with Allah",
                color = Color(0xFFFFD54F),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Janamaz Verification Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF11251D),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Janamaz AI Authorization",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF198754).copy(alpha = 0.25f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF4EE0A8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "gemini-3.1-pro-preview",
                                    fontSize = 10.sp,
                                    color = Color(0xFF4EE0A8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To unlock your apps, place your Janamaz (prayer mat) on the floor and take or upload a photo. Gemini AI will analyze the rug patterns and orientation to authorize your prayer.",
                        color = Color(0xFFC0C9C2),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photo preview or Placeholder
                    val photo = uiState.capturedPhoto
                    if (photo != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.dp, Color(0xFFE5C158), RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                bitmap = photo.asImageBitmap(),
                                contentDescription = "Captured Janamaz",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Retake icon overlay
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                TextButton(
                                    onClick = { cameraLauncher.launch(null) }
                                ) {
                                    Text("Retake", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        // Empty photo target
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0A1813))
                                .border(1.dp, Color(0xFF1E3A2E), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera placeholder",
                                    tint = Color(0xFF4EE0A8),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Photo Selected",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Capture or pick a photo of your Janamaz",
                                    color = Color(0xFF82948B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Camera & Gallery action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("camera_capture_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gallery_pick_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", color = Color.White)
                        }
                    }

                    // Analyzing progress
                    AnimatedVisibility(visible = uiState.isAnalyzing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF4EE0A8)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.analysisStatusText,
                                color = Color(0xFFE5C158),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Verification Result display
                    val result = uiState.verificationResult
                    if (result != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (result.isJanamaz) Color(0xFF198754).copy(alpha = 0.2f) else Color(0xFFD32F2F).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (result.isJanamaz) Color(0xFF198754) else Color(0xFFD32F2F)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (result.isJanamaz) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (result.isJanamaz) Color(0xFF4EE0A8) else Color(0xFFFF8A80),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (result.isJanamaz) "Janamaz Verified (${result.confidence}%)" else "Verification Failed (${result.confidence}%)",
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.isJanamaz) Color(0xFF4EE0A8) else Color(0xFFFF8A80),
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.summary,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (result.details.isNotBlank()) {
                                    Text(
                                        text = result.details,
                                        color = Color(0xFFC0C9C2),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Verify Button
                    if (photo != null && !uiState.isAnalyzing && (result == null || !result.isJanamaz)) {
                        Spacer(modifier = Modifier.height(14.dp))
                        ElevatedButton(
                            onClick = onVerifyPhoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_janamaz_button"),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFF0E5C44),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyze with Gemini AI & Unlock",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Skip / Leave Namaz Penalty Section
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF141F1A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (freeSkips > 0) Color(0xFFFFD54F) else Color(0xFFFF8A80),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Leave Namaz Policy",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (freeSkips > 0) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color(0xFFD32F2F).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (freeSkips > 0) "$freeSkips / 10 Free Skips Left" else "0 Free Skips (₹10 Fine)",
                                color = if (freeSkips > 0) Color(0xFFFFD54F) else Color(0xFFFF8A80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (freeSkips > 0)
                            "Each user is given strictly 10 free chances to leave Namaz. Skipping now will consume 1 chance ($freeSkips remaining)."
                        else
                            "All 10 free chances have been consumed! You must pay a penalty fine of ₹10 via UPI to unlock without praying.",
                        color = Color(0xFF9EA9A1),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            if (freeSkips > 0) {
                                showSkipConfirmDialog = true
                            } else {
                                onSkipRequested(prayer)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leave_namaz_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (freeSkips > 0) Color(0xFFFFD54F) else Color(0xFFFF8A80)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (freeSkips > 0) "Leave Namaz (Deduct 1 Free Chance)" else "Pay ₹10 Penalty to Unlock",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for using 1 of 10 free chances
    if (showSkipConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSkipConfirmDialog = false },
            title = {
                Text("Confirm Leaving Namaz?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "You are about to skip ${prayer.displayName} prayer.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This will use 1 of your remaining $freeSkips free chances. Once exhausted, each skipped namaz requires a payment of ₹10.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSkipConfirmDialog = false
                        onSkipRequested(prayer)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_skip_button")
                ) {
                    Text("Skip (-1 Chance)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipConfirmDialog = false }) {
                    Text("Cancel (Offer Namaz)")
                }
            }
        )
    }

    // Payment Dialog when free chances = 0
    if (uiState.showPaymentSheet) {
        val targetPrayer = uiState.targetPrayerForPayment ?: prayer
        PaymentDialog(
            prayerType = targetPrayer,
            isProcessing = uiState.isAnalyzing,
            onDismiss = onDismissPayment,
            onConfirmPayment = onConfirmPayment
        )
    }
}
