package com.example.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

/**
 * High-priority Foreground Service guarding active Salah lockdown.
 * If user attempts to swipe away the app from Recent tasks, [onTaskRemoved]
 * intercepts the closure and immediately relaunches Strict Salah back to front.
 */
class LockdownForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "strict_salah_lockdown_service"
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_LOCKDOWN = "com.example.ACTION_START_LOCKDOWN"
        const val ACTION_STOP_LOCKDOWN = "com.example.ACTION_STOP_LOCKDOWN"
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"

        fun startService(context: Context, prayerName: String = "Salah") {
            try {
                val intent = Intent(context, LockdownForegroundService::class.java).apply {
                    action = ACTION_START_LOCKDOWN
                    putExtra(EXTRA_PRAYER_NAME, prayerName)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore foreground service start restrictions
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, LockdownForegroundService::class.java).apply {
                    action = ACTION_STOP_LOCKDOWN
                }
                context.stopService(intent)
            } catch (e: Exception) {
                // Handled
            }
        }
    }

    private var activePrayer: String = "Salah"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_LOCKDOWN) {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (e: Exception) {
                // Handled
            }
            stopSelf()
            return START_NOT_STICKY
        }

        activePrayer = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        try {
            val notification = buildLockdownNotification(activePrayer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // Safe fallback
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // User swiped the app away from Recent Apps!
        // Immediately relaunch Strict Salah to prevent lockdown bypass
        val relaunchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra("TRIGGER_LOCKDOWN", true)
            putExtra("PRAYER_TYPE", activePrayer.lowercase())
        }

        try {
            startActivity(relaunchIntent)
        } catch (e: Exception) {
            // Fallback pending intent launch
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                8888,
                relaunchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                pendingIntent.send()
            } catch (e2: Exception) {
                // Handled
            }
        }

        // Post high-priority notification to pull back
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, buildLockdownNotification(activePrayer))
        } catch (e: Exception) {
            // Handled
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Strict Salah Anti-Close Guard",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Guards active Salah lockdown to prevent closing before prayer is verified"
                enableVibration(true)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildLockdownNotification(prayerName: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TRIGGER_LOCKDOWN", true)
            putExtra("PRAYER_TYPE", prayerName.lowercase())
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            9001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("🔒 Strict Salah Active: $prayerName Time")
            .setContentText("App cannot be closed until Janamaz is verified or prayer penalty paid.")
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(
                android.R.drawable.ic_menu_camera,
                "Open & Verify Janamaz",
                pendingIntent
            )
            .build()
    }
}
