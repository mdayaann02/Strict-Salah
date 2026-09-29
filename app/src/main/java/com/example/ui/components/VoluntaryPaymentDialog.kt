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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.payment.UpiPaymentGateway
import com.example.ui.theme.LiquidAqua
import com.example.ui.theme.LiquidEmerald

@Composable
fun VoluntaryPaymentDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, note: String, paymentApp: String, upiRef: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recipientUpiId = UpiPaymentGateway.OFFICIAL_UPI_ID

    var selectedAmount by remember { mutableDoubleStateOf(50.0) }
    var customAmountText by remember { mutableStateOf("50") }
    var paymentNote by remember { mutableStateOf("Sadaqah / Prayer Pledge") }
    var selectedApp by remember { mutableStateOf("Google Pay") }
    var upiRefInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showManualUtrField by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }

    val presetAmounts = listOf(10.0, 50.0, 100.0, 500.0)
    val upiApps = listOf(
        Triple("Google Pay", "com.google.android.apps.nbu.paisa.user", Color(0xFF1A73E8)),
        Triple("PhonePe", "com.phonepe.app", Color(0xFF5F259F)),
        Triple("Paytm", "net.one97.paytm", Color(0xFF00B9F5)),
        Triple("BHIM UPI", "in.org.npci.upiapp", Color(0xFF005696))
    )

    val qrCodeUrl = remember(selectedAmount, paymentNote) {
        UpiPaymentGateway.getUpiQrCodeUrl(selectedAmount, paymentNote)
    }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val parsed = UpiPaymentGateway.parseUpiResponseIntent(result.data)
        if (parsed.isSuccess && !parsed.transactionId.isNullOrBlank()) {
            val ref = parsed.transactionId!!
            upiRefInput = ref
            statusMessage = "✅ Payment approved via UPI Intent! Ref: $ref"
            onConfirmPayment(selectedAmount, paymentNote, selectedApp, ref)
        } else {
            statusMessage = "Returned from UPI app. Tap 'Confirm Payment' to record your pledge."
        }
    }

    fun launchDirectUpiApp(pkg: String?, appName: String) {
        selectedApp = appName
        try {
            if (pkg != null && !UpiPaymentGateway.isAppInstalled(context, pkg)) {
                val genericIntent = UpiPaymentGateway.createPaymentIntent(
                    amount = selectedAmount,
                    note = paymentNote
                )
                val chooser = Intent.createChooser(genericIntent, "Pay ₹${selectedAmount.toInt()} via UPI")
                upiLauncher.launch(chooser)
                return
            }
            val intent = UpiPaymentGateway.createPaymentIntent(
                amount = selectedAmount,
                note = paymentNote,
                targetPackage = pkg
            )
            upiLauncher.launch(intent)
        } catch (e: Exception) {
            try {
                val genericIntent = UpiPaymentGateway.createPaymentIntent(
                    amount = selectedAmount,
                    note = paymentNote
                )
                val chooser = Intent.createChooser(genericIntent, "Pay ₹${selectedAmount.toInt()} via UPI")
                upiLauncher.launch(chooser)
            } catch (err: Exception) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Strict Salah UPI", recipientUpiId))
                Toast.makeText(context, "UPI ID copied: $recipientUpiId", Toast.LENGTH_LONG).show()
                showQrCode = true
                statusMessage = "No UPI app opened. UPI ID copied! Scan QR or pay via your bank app."
            }
        }
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Strict Salah UPI ID", recipientUpiId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "UPI ID copied: $recipientUpiId", Toast.LENGTH_SHORT).show()
    }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        // Apple Music Liquid Frosted Glass Modal
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0B1720).copy(alpha = 0.95f),
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
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
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Direct UPI Payment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Sadaqah, Fines & Discipline Pledges",
                                fontSize = 11.sp,
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Selection Chips
                Text(
                    text = "SELECT AMOUNT (INR ₹)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amt ->
                        val isSelected = selectedAmount == amt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAmount = amt
                                    customAmountText = amt.toInt().toString()
                                }
                        ) {
                            Text(
                                text = "₹${amt.toInt()}",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF6EE7B7) else Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom amount & Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = {
                            customAmountText = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                selectedAmount = parsed
                            }
                        },
                        label = { Text("Amount (₹)", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(0.42f)
                    )
                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Purpose / Note", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(0.58f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Official Receiver card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Official Receiver UPI ID:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(recipientUpiId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { copyToClipboard() }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAYMENT OPTIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (showQrCode) Color(0xFF10B981).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showQrCode) Color(0xFF10B981) else Color.White.copy(alpha = 0.2f)),
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
                                contentDescription = "UPI QR Code for ₹${selectedAmount.toInt()}",
                                modifier = Modifier.size(184.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan with Any UPI App to Pay ₹${selectedAmount.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Direct Launch App Row
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
                                Icon(Icons.Default.OpenInNew, contentDescription = appName, tint = color, modifier = Modifier.size(18.dp))
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

                Spacer(modifier = Modifier.height(8.dp))

                // Chooser button
                Button(
                    onClick = { launchDirectUpiApp(null, "UPI App") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pay_via_upi_intent_button")
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Redirect to UPI App (₹${selectedAmount.toInt()})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F766E).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage!!,
                            fontSize = 11.sp,
                            color = Color(0xFFCCFBF1),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Payment Button
                Button(
                    onClick = {
                        val ref = upiRefInput.trim().ifBlank {
                            "SS-PLEDGE-${System.currentTimeMillis() % 1000000}"
                        }
                        onConfirmPayment(selectedAmount, paymentNote, selectedApp, ref)
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recording Payment...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm ₹${selectedAmount.toInt()} Paid", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
