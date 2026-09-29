package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.PrayerType
import com.example.data.payment.UpiPaymentGateway
import com.example.ui.theme.LiquidAqua

@Composable
fun PaymentDialog(
    prayerType: PrayerType,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentApp: String, upiId: String) -> Unit
) {
    val context = LocalContext.current
    val recipientUpiId = UpiPaymentGateway.OFFICIAL_UPI_ID
    val skipAmount = UpiPaymentGateway.PRAYER_SKIP_PENALTY_AMOUNT

    var selectedApp by remember { mutableStateOf("Google Pay") }
    var upiRefInput by remember { mutableStateOf("") }
    var upiStatusMessage by remember { mutableStateOf<String?>(null) }
    var showManualUtrField by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }

    val upiApps = listOf(
        Triple("Google Pay", "com.google.android.apps.nbu.paisa.user", Color(0xFF1A73E8)),
        Triple("PhonePe", "com.phonepe.app", Color(0xFF5F259F)),
        Triple("Paytm", "net.one97.paytm", Color(0xFF00B9F5)),
        Triple("BHIM UPI", "in.org.npci.upiapp", Color(0xFF005696))
    )

    val qrCodeUrl = remember {
        UpiPaymentGateway.getUpiQrCodeUrl(skipAmount, "Strict Salah Skip ${prayerType.name}")
    }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val parsed = UpiPaymentGateway.parseUpiResponseIntent(result.data)
        if (parsed.isSuccess && !parsed.transactionId.isNullOrBlank()) {
            val ref = parsed.transactionId!!
            upiRefInput = ref
            upiStatusMessage = "✅ Payment approved via UPI app! Ref: $ref"
            onConfirmPayment(selectedApp, ref)
        } else {
            upiStatusMessage = "Returned from UPI app. Tap 'I Have Paid ₹10 (Verify & Unlock)' below to continue."
        }
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Strict Salah UPI ID", recipientUpiId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "UPI ID copied: $recipientUpiId", Toast.LENGTH_SHORT).show()
    }

    fun launchDirectUpiApp(pkg: String?, appName: String) {
        selectedApp = appName
        try {
            val note = "Strict Salah Skip ${prayerType.name}"
            if (pkg != null && !UpiPaymentGateway.isAppInstalled(context, pkg)) {
                // Not installed, fall back to chooser immediately
                val genericIntent = UpiPaymentGateway.createPaymentIntent(amount = skipAmount, note = note)
                val chooser = Intent.createChooser(genericIntent, "Pay ₹10 with any UPI App")
                upiLauncher.launch(chooser)
                return
            }
            val intent = UpiPaymentGateway.createPaymentIntent(
                amount = skipAmount,
                note = note,
                targetPackage = pkg
            )
            upiLauncher.launch(intent)
        } catch (e: Exception) {
            try {
                val genericIntent = UpiPaymentGateway.createPaymentIntent(
                    amount = skipAmount,
                    note = "Strict Salah Skip ${prayerType.name}"
                )
                val chooser = Intent.createChooser(genericIntent, "Pay ₹10 with $appName")
                upiLauncher.launch(chooser)
            } catch (err: Exception) {
                copyToClipboard()
                showQrCode = true
                upiStatusMessage = "No UPI app found. UPI ID copied! Scan QR or pay ₹10 via your bank app."
            }
        }
    }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(28.dp),
            glowColor = LiquidAqua,
            modifier = Modifier.fillMaxWidth()
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
                            color = Color(0xFFD32F2F).copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Pay ₹10 Skip Fine",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AppleMusicWaveVisualizer(color = LiquidAqua)
                            }
                            Text(
                                text = "Free Quota Exhausted (0/3)",
                                fontSize = 11.sp,
                                color = Color(0xFFFF8A80),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apple Music Frosted Summary Card
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    glowColor = Color(0xFF00E5FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DISCIPLINE PENALTY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = "₹10.00",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = prayerType.displayName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Receiver UPI ID row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Official Payee UPI ID:",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = recipientUpiId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { copyToClipboard() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Copy", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle QR Code vs App Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHOOSE PAYMENT METHOD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (showQrCode) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showQrCode) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable { showQrCode = !showQrCode }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showQrCode) "Hide QR" else "Show QR", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = showQrCode) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = qrCodeUrl,
                                contentDescription = "UPI QR Code for ₹10",
                                modifier = Modifier.size(184.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan with Any UPI App to Pay ₹10",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // One-tap direct launch app grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    upiApps.forEach { (appName, pkg, color) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = color.copy(alpha = 0.22f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { launchDirectUpiApp(pkg, appName) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = appName,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = appName.split(" ")[0],
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Or open any UPI App chooser button
                Button(
                    onClick = { launchDirectUpiApp(null, "UPI App") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("launch_upi_app_button")
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Any UPI App (₹10)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (upiStatusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = upiStatusMessage!!,
                            fontSize = 11.sp,
                            color = Color(0xFFE0F2FE),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Unlock Button: Always responsive and never gets stuck
                Button(
                    onClick = {
                        val ref = upiRefInput.trim().ifBlank {
                            "SS-UPI-${System.currentTimeMillis() % 1000000}"
                        }
                        onConfirmPayment(selectedApp, ref)
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pay_fine_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying Payment...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("I Have Paid ₹10 (Verify & Unlock)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Optional UTR Reference toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { showManualUtrField = !showManualUtrField }) {
                        Text(
                            text = if (showManualUtrField) "Hide 12-Digit UTR Field" else "Attach 12-Digit UTR Reference (Optional)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                AnimatedVisibility(visible = showManualUtrField) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        OutlinedTextField(
                            value = upiRefInput,
                            onValueChange = { upiRefInput = it },
                            label = { Text("Bank UTR / Txn Reference (Optional)", color = Color.White.copy(alpha = 0.7f)) },
                            placeholder = { Text("e.g. 427819827102", color = Color.White.copy(alpha = 0.4f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("upi_id_input")
                        )
                    }
                }
            }
        }
    }
}
