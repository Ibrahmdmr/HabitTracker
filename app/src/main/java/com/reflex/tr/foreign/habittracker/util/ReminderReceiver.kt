package com.reflex.tr.foreign.habittracker.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.reflex.tr.foreign.habittracker.R
import com.reflex.tr.foreign.habittracker.data.model.ReminderSettings

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_SHOW_REMINDER) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)
        val settings = ReminderSettings(
            notificationsEnabled = intent?.getBooleanExtra(EXTRA_NOTIFICATIONS_ENABLED, true) ?: true,
            reminderHour = intent?.getIntExtra(EXTRA_REMINDER_HOUR, 20) ?: 20,
            reminderMinute = intent?.getIntExtra(EXTRA_REMINDER_MINUTE, 0) ?: 0
        )
        val shouldScheduleNextDay = intent?.getBooleanExtra(EXTRA_SCHEDULE_NEXT_DAY, false) ?: false
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        if (shouldScheduleNextDay) {
            ReminderScheduler(context).scheduleNextDay(settings)
        }
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.reflex.tr.foreign.habittracker.SHOW_REMINDER"
        const val EXTRA_NOTIFICATIONS_ENABLED = "extra_notifications_enabled"
        const val EXTRA_REMINDER_HOUR = "extra_reminder_hour"
        const val EXTRA_REMINDER_MINUTE = "extra_reminder_minute"
        const val EXTRA_SCHEDULE_NEXT_DAY = "extra_schedule_next_day"
        const val CHANNEL_ID = "daily_reminder_channel"
        private const val NOTIFICATION_ID = 1001

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
