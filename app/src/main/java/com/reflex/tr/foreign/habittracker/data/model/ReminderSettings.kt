package com.reflex.tr.foreign.habittracker.data.model

data class ReminderSettings(
    val notificationsEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0
)
