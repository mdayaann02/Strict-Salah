package com.example.data.payment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.util.Locale
import java.util.UUID

data class UpiAppInfo(
    val name: String,
    val packageName: String,
    val isInstalled: Boolean
)

data class UpiPaymentResult(
    val isSuccess: Boolean,
    val transactionId: String?,
    val referenceId: String?,
    val responseCode: String?,
    val status: String,
    val rawResponse: String?
)

object UpiPaymentGateway {

    const val OFFICIAL_UPI_ID = "8217317725@superyes"
    const val PAYEE_NAME = "Strict Salah Discipline"
    const val UNINSTALL_PENALTY_AMOUNT = 100.00
    const val PRAYER_SKIP_PENALTY_AMOUNT = 10.00

    val KNOWN_UPI_PACKAGES = listOf(
        Pair("Google Pay", "com.google.android.apps.nbu.paisa.user"),
        Pair("PhonePe", "com.phonepe.app"),
        Pair("Paytm", "net.one97.paytm"),
        Pair("BHIM UPI", "in.org.npci.upiapp"),
        Pair("Amazon Pay", "in.amazon.mShop.android.shopping"),
        Pair("CRED", "com.dreamplug.androidapp")
    )

    fun getInstalledUpiApps(context: Context): List<UpiAppInfo> {
        val packageManager = context.packageManager
        return KNOWN_UPI_PACKAGES.map { (name, pkg) ->
            val isInstalled = try {
                packageManager.getPackageInfo(pkg, 0)
                true
            } catch (e: Exception) {
                false
            }
            UpiAppInfo(name = name, packageName = pkg, isInstalled = isInstalled)
        }
    }

    fun buildUpiUri(
        amount: Double,
        note: String,
        transactionRef: String = "SS${System.currentTimeMillis() % 10000000}"
    ): Uri {
        val amountStr = String.format(Locale.US, "%.2f", amount)
        val encodedNote = Uri.encode(note)
        val encodedName = Uri.encode(PAYEE_NAME)
        val uriString = "upi://pay?pa=$OFFICIAL_UPI_ID&pn=$encodedName&mc=&tid=${System.currentTimeMillis()}&tr=$transactionRef&tn=$encodedNote&am=$amountStr&cu=INR"
        return Uri.parse(uriString)
    }

    fun createPaymentIntent(
        amount: Double,
        note: String,
        targetPackage: String? = null,
        transactionRef: String = "SS${System.currentTimeMillis() % 10000000}"
    ): Intent {
        val uri = buildUpiUri(amount, note, transactionRef)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (!targetPackage.isNullOrBlank()) {
            intent.setPackage(targetPackage)
        }
        return intent
    }

    /**
     * Parses the response returned from UPI apps via ActivityResult Intent.
     * Strictly verifies explicit SUCCESS status code and transaction ID.
     */
    fun parseUpiResponseIntent(intent: Intent?): UpiPaymentResult {
        if (intent == null) {
            return UpiPaymentResult(
                isSuccess = false,
                transactionId = null,
                referenceId = null,
                responseCode = null,
                status = "AWAITING_VERIFICATION",
                rawResponse = null
            )
        }

        // 1. Try "response" extra
        val responseExtra = intent.getStringExtra("response")
        if (!responseExtra.isNullOrBlank()) {
            return parseUpiResponse(responseExtra)
        }

        // 2. Try URI data string
        val dataUriString = intent.dataString
        if (!dataUriString.isNullOrBlank() && dataUriString.contains("Status", ignoreCase = true)) {
            val query = Uri.parse(dataUriString).query
            if (!query.isNullOrBlank()) {
                return parseUpiResponse(query)
            }
        }

        // 3. Try direct extras
        val statusExtra = intent.getStringExtra("Status") ?: intent.getStringExtra("status")
        val txnId = intent.getStringExtra("txnId") ?: intent.getStringExtra("ApprovalRefNo")
        val refId = intent.getStringExtra("txnRef") ?: intent.getStringExtra("tr")
        val respCode = intent.getStringExtra("responseCode")

        if (!statusExtra.isNullOrBlank()) {
            val statusUpper = statusExtra.uppercase(Locale.ROOT)
            val isSuccess = (statusUpper == "SUCCESS" || respCode == "00") && statusUpper != "FAILURE" && statusUpper != "FAILED" && statusUpper != "CANCELLED"
            return UpiPaymentResult(
                isSuccess = isSuccess,
                transactionId = txnId,
                referenceId = refId,
                responseCode = respCode,
                status = statusUpper,
                rawResponse = "Status=$statusExtra&txnId=$txnId&txnRef=$refId&responseCode=$respCode"
            )
        }

        return UpiPaymentResult(
            isSuccess = false,
            transactionId = null,
            referenceId = null,
            responseCode = null,
            status = "AWAITING_VERIFICATION",
            rawResponse = null
        )
    }

    fun parseUpiResponse(responseString: String?): UpiPaymentResult {
        if (responseString.isNullOrBlank()) {
            return UpiPaymentResult(
                isSuccess = false,
                transactionId = null,
                referenceId = null,
                responseCode = null,
                status = "CANCELLED_OR_EMPTY",
                rawResponse = responseString
            )
        }

        val params = mutableMapOf<String, String>()
        val pairs = responseString.split("&")
        for (pair in pairs) {
            val parts = pair.split("=")
            if (parts.size >= 2) {
                params[parts[0].trim().lowercase(Locale.ROOT)] = parts[1].trim()
            }
        }

        val status = params["status"]?.uppercase(Locale.ROOT) ?: "UNKNOWN"
        val txnId = params["txnid"] ?: params["approvalrefno"]
        val refId = params["txnref"]
        val responseCode = params["responsecode"]

        // Strictly verify SUCCESS - never accept FAILURE, CANCELLED, or UNKNOWN
        val isSuccess = (status == "SUCCESS" || responseCode == "00") && status != "FAILURE" && status != "FAILED" && status != "CANCELLED"

        return UpiPaymentResult(
            isSuccess = isSuccess,
            transactionId = txnId,
            referenceId = refId,
            responseCode = responseCode,
            status = status,
            rawResponse = responseString
        )
    }

    fun isValidUtrReference(utr: String): Boolean {
        val cleaned = utr.trim()
        // Standard bank UTR / UPI reference is typically 12 alphanumeric characters or digits
        return cleaned.length >= 6 && !cleaned.contains(" ")
    }

    fun generateUninstallToken(): String {
        return "SS-UNINSTALL-" + UUID.randomUUID().toString().take(8).uppercase(Locale.ROOT) + "-" + (System.currentTimeMillis() % 10000)
    }

    fun generateReceiptNumber(type: String): String {
        return "SS-${type.uppercase(Locale.ROOT)}-${System.currentTimeMillis() % 1000000}"
    }
}
