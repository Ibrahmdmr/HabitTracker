package com.reflex.tr.foreign.habittracker

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflex.tr.foreign.habittracker.presentation.navigation.HabitNavGraph
import com.reflex.tr.foreign.habittracker.presentation.screen.HabitViewModel
import com.reflex.tr.foreign.habittracker.presentation.theme.HabitTrackerTheme
import com.reflex.tr.foreign.habittracker.util.ReminderReceiver

class MainActivity : ComponentActivity() {
    private var notificationPermissionResult: ((Boolean, Boolean) -> Unit)? = null
    private var exactAlarmPermissionResult: ((Boolean) -> Unit)? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val permanentlyDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !granted &&
            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        notificationPermissionResult?.invoke(granted, permanentlyDenied)
        notificationPermissionResult = null
    }

    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        exactAlarmPermissionResult?.invoke(canScheduleExactAlarms())
        exactAlarmPermissionResult = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderReceiver.ensureChannel(this)
        setContent {
            val viewModel: HabitViewModel = viewModel()
            HabitTrackerTheme {
                HabitNavGraph(
                    viewModel = viewModel,
                    onNotificationPermissionRequest = ::requestNotificationPermission,
                    onExactAlarmPermissionRequest = ::requestExactAlarmPermission
                )
            }
        }
    }

    private fun requestNotificationPermission(onResult: (Boolean, Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onResult(true, false)
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            onResult(true, false)
        } else {
            notificationPermissionResult = onResult
            notificationPermissionLauncher.launch(permission)
        }
    }

    private fun requestExactAlarmPermission(onResult: (Boolean) -> Unit) {
        if (canScheduleExactAlarms()) {
            onResult(true)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            exactAlarmPermissionResult = onResult
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            exactAlarmPermissionLauncher.launch(intent)
        } else {
            onResult(true)
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }
}
