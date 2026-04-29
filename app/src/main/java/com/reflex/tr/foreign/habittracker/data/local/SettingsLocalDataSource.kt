package com.reflex.tr.foreign.habittracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reflex.tr.foreign.habittracker.data.model.ReminderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "habit_settings")

class SettingsLocalDataSource(context: Context) {
    private val dataStore = context.settingsDataStore
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")

    val settings: Flow<ReminderSettings> = dataStore.data.map { preferences ->
        ReminderSettings(
            notificationsEnabled = preferences[notificationsEnabledKey] ?: false,
            reminderHour = preferences[reminderHourKey] ?: 20,
            reminderMinute = preferences[reminderMinuteKey] ?: 0
        )
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }

    suspend fun saveReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[reminderHourKey] = hour
            preferences[reminderMinuteKey] = minute
        }
    }
}
