package com.reflex.tr.foreign.habittracker.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.reflex.tr.foreign.habittracker.BuildConfig
import com.reflex.tr.foreign.habittracker.data.model.ReminderSettings
import java.util.Calendar

class ReminderScheduler(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(settings: ReminderSettings) {
        if (!settings.notificationsEnabled) {
            cancel()
            return
        }
        if (!hasNotificationPermission()) {
            cancel()
            return
        }
        cancel()

        ReminderReceiver.ensureChannel(context)
        val triggerAt = nextTriggerAt(settings.reminderHour, settings.reminderMinute)
        setAlarm(triggerAt, pendingIntent(settings, scheduleNextDay = true))
    }

    fun scheduleNextDay(settings: ReminderSettings) {
        if (!settings.notificationsEnabled) return
        if (!hasNotificationPermission()) return

        val triggerAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, settings.reminderHour)
            set(Calendar.MINUTE, settings.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        setAlarm(triggerAt, pendingIntent(settings, scheduleNextDay = true))
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(triggerAt: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            setInexactAlarm(triggerAt, pendingIntent)
            return
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (exception: SecurityException) {
            setInexactAlarm(triggerAt, pendingIntent)
        }
    }

    private fun setInexactAlarm(triggerAt: Long, pendingIntent: PendingIntent) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent())
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun nextTriggerAt(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sameMinute = now.get(Calendar.HOUR_OF_DAY) == hour && now.get(Calendar.MINUTE) == minute
        if (BuildConfig.DEBUG && sameMinute && trigger.timeInMillis <= now.timeInMillis) {
            return now.timeInMillis + SAME_MINUTE_DEBUG_DELAY_MS
        }
        if (trigger.timeInMillis <= now.timeInMillis) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }

    private fun pendingIntent(
        settings: ReminderSettings? = null,
        requestCode: Int = REMINDER_REQUEST_CODE,
        scheduleNextDay: Boolean = true
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SHOW_REMINDER
            putExtra(ReminderReceiver.EXTRA_SCHEDULE_NEXT_DAY, scheduleNextDay)
            settings?.let {
                putExtra(ReminderReceiver.EXTRA_NOTIFICATIONS_ENABLED, it.notificationsEnabled)
                putExtra(ReminderReceiver.EXTRA_REMINDER_HOUR, it.reminderHour)
                putExtra(ReminderReceiver.EXTRA_REMINDER_MINUTE, it.reminderMinute)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val REMINDER_REQUEST_CODE = 42
        private const val SAME_MINUTE_DEBUG_DELAY_MS = 5_000L
    }
}
