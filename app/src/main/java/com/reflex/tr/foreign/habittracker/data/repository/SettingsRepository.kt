package com.reflex.tr.foreign.habittracker.data.repository

import com.reflex.tr.foreign.habittracker.data.local.SettingsLocalDataSource
import com.reflex.tr.foreign.habittracker.data.model.ReminderSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val localDataSource: SettingsLocalDataSource
) {
    val settings: Flow<ReminderSettings> = localDataSource.settings

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        localDataSource.saveNotificationsEnabled(enabled)
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        localDataSource.saveReminderTime(hour, minute)
    }
}
