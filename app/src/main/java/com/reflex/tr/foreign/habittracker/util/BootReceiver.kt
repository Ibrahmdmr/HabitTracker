package com.reflex.tr.foreign.habittracker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reflex.tr.foreign.habittracker.data.local.SettingsLocalDataSource
import com.reflex.tr.foreign.habittracker.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderReceiver.ensureChannel(context)
                val settingsRepository = SettingsRepository(SettingsLocalDataSource(context))
                val settings = settingsRepository.settings.first()
                if (settings.notificationsEnabled) {
                    ReminderScheduler(context).schedule(settings)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
