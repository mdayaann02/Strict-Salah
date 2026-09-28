package com.example.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.payment.UpiPaymentGateway

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

    val presetAmounts = listOf(10.0, 50.0, 100.0, 500.0)
    val upiApps = listOf(
        Pair("Google Pay", Color(0xFF1A73E8)),
        Pair("PhonePe", Color(0xFF5F259F)),
        Pair("Paytm", Color(0xFF00B9F5)),
        Pair("BHIM UPI", Color(0xFF005696))
    )

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val parsed = UpiPaymentGateway.parseUpiResponseIntent(result.data)
        if (parsed.isSuccess || result.resultCode == Activity.RESULT_OK) {
            val ref = parsed.transactionId ?: parsed.referenceId ?: "UPI-PAY-${System.currentTimeMillis() % 1000000}"
            upiRefInput = ref
            statusMessage = "✅ Payment approved via UPI Intent! Ref: $ref"
            onConfirmPayment(selectedAmount, paymentNote, selectedApp, ref)
        } else {
            statusMessage = "Returned from UPI app (${parsed.status}). Enter UTR reference if paid."
        }
    }

    fun launchUpiIntent(targetPackage: String? = null) {
        try {
            val intent = UpiPaymentGateway.createPaymentIntent(
                amount = selectedAmount,
                note = paymentNote,
                targetPackage = targetPackage
            )
            val chooser = Intent.createChooser(intent, "Pay ₹${selectedAmount.toInt()} via UPI")
            upiLauncher.launch(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No UPI app found. Please copy UPI ID: $recipientUpiId", Toast.LENGTH_LONG).show()
        }
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Strict Salah UPI ID", recipientUpiId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "UPI ID copied: $recipientUpiId", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = "Payment",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Make Payment / Pledge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Sadaqah, Kaffarah & Discipline Pledge",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select Contribution Amount (INR ₹):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Preset amount chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amt ->
                        val isSelected = selectedAmount == amt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedAmount = amt
                                    customAmountText = amt.toInt().toString()
                                }
                        ) {
                            Text(
                                text = "₹${amt.toInt()}",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
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
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(0.45f)
                    )
                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Purpose / Note") },
                        singleLine = true,
                        modifier = Modifier.weight(0.55f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receiver UPI card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Receiver UPI ID:",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = recipientUpiId,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Strict Salah Discipline & Charity Fund",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { copyToClipboard() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UPI ID",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Open in UPI app button
                Button(
                    onClick = { launchUpiIntent() },
                    modifier = Modifier.fillMaxWidth().testTag("pay_via_upi_intent_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Redirect to UPI App (₹${selectedAmount.toInt()})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusMessage!!,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Or Select App & Enter UTR / Txn Reference:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // App selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    upiApps.forEach { (appName, appColor) ->
                        val isSelected = selectedApp == appName
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) appColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) appColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedApp = appName }
                        ) {
                            Text(
                                text = appName.split(" ")[0],
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) appColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = upiRefInput,
                    onValueChange = { upiRefInput = it },
                    label = { Text("12-Digit UTR / Transaction ID") },
                    placeholder = { Text("e.g. 427819827102") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ref = upiRefInput.trim().ifBlank { "UPI-PAY-${System.currentTimeMillis() % 1000000}" }
                    onConfirmPayment(selectedAmount, paymentNote, selectedApp, ref)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recording...")
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm Payment (₹${selectedAmount.toInt()})")
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
