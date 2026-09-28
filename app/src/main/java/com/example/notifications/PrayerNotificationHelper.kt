package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerType

object PrayerNotificationHelper {

    private const val CHANNEL_ID = "salah_prayer_reminders"
    private const val CHANNEL_NAME = "Salah Prayer Reminders & Lock"
    private const val NOTIFICATION_ID_BASE = 5000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Timely reminders and lockdown notifications for Islamic Salah prayers"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPrayerReminder(
        context: Context,
        prayerType: PrayerType,
        minutesRemaining: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_PRAYER", prayerType.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerType.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 ${prayerType.displayName} Prayer in $minutesRemaining Minutes")
            .setContentText("Prepare for ${prayerType.arabicName}. All apps will be locked when prayer starts.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + prayerType.ordinal, notification)
        } catch (e: SecurityException) {
            // Notification permission might be pending
        }
    }

    fun showLockdownActiveNotification(
        context: Context,
        prayerType: PrayerType
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TRIGGER_LOCKDOWN", true)
            putExtra("PRAYER_TYPE", prayerType.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("🚨 Salah Lockdown: ${prayerType.displayName} Time!")
            .setContentText("Apps locked. Lay down your Janamaz and snap a photo to verify.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_camera,
                "Verify Janamaz",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + 99, notification)
        } catch (e: SecurityException) {
            // Permission handling
        }
    }

    fun clearLockdownNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + 99)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
