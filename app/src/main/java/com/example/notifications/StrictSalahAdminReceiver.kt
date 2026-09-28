package com.example.notifications

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class StrictSalahAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "🛡️ Strict Salah Device Protection Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "⚠️ Strict Salah Accountability Warning: Disabling protection or uninstalling requires fulfilling your ₹100 commitment pledge."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Strict Salah Device Protection Deactivated", Toast.LENGTH_SHORT).show()
    }
}
