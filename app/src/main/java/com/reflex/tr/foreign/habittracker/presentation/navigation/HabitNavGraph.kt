package com.reflex.tr.foreign.habittracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reflex.tr.foreign.habittracker.R
import com.reflex.tr.foreign.habittracker.presentation.screen.AddHabitScreen
import com.reflex.tr.foreign.habittracker.presentation.screen.HabitDetailScreen
import com.reflex.tr.foreign.habittracker.presentation.screen.HabitViewModel
import com.reflex.tr.foreign.habittracker.presentation.screen.HomeScreen
import com.reflex.tr.foreign.habittracker.presentation.screen.InfoScreen
import com.reflex.tr.foreign.habittracker.presentation.screen.SettingsScreen
import kotlinx.coroutines.launch

object Routes {
    const val Home = "home"
    const val AddHabit = "addHabit"
    const val Detail = "habitDetail/{habitId}"
    const val Settings = "settings"
    const val Info = "info"

    fun detail(habitId: Long) = "habitDetail/$habitId"
}

@Composable
fun HabitNavGraph(
    viewModel: HabitViewModel,
    onNotificationPermissionRequest: ((Boolean, Boolean) -> Unit) -> Unit,
    onExactAlarmPermissionRequest: ((Boolean) -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable(Routes.Home) {
            val habitAddedMessage = stringResource(R.string.habit_added_snackbar)
            val habitDeletedMessage = stringResource(R.string.habit_deleted_snackbar)
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

            LaunchedEffect(savedStateHandle) {
                savedStateHandle
                    ?.getStateFlow("habitAdded", false)
                    ?.collect { habitAdded ->
                        if (habitAdded) {
                            snackbarHostState.showSnackbar(habitAddedMessage)
                            savedStateHandle.set("habitAdded", false)
                        }
                    }
            }
            LaunchedEffect(savedStateHandle) {
                savedStateHandle
                    ?.getStateFlow("habitDeleted", false)
                    ?.collect { habitDeleted ->
                        if (habitDeleted) {
                            snackbarHostState.showSnackbar(habitDeletedMessage)
                            savedStateHandle.set("habitDeleted", false)
                        }
                    }
            }

            HomeScreen(
                uiState = uiState,
                onAddHabitClick = { navController.navigate(Routes.AddHabit) },
                onHabitClick = { navController.navigate(Routes.detail(it)) },
                onToggleHabit = viewModel::toggleToday,
                onCountChange = viewModel::changeTodayCount,
                onSettingsClick = { navController.navigate(Routes.Settings) },
                snackbarHostState = snackbarHostState
            )
        }
        composable(Routes.AddHabit) {
            AddHabitScreen(
                onBack = { navController.popBackStack() },
                onSave = { name, emoji, type, targetCount, unit ->
                    viewModel.addHabit(name, emoji, type, targetCount, unit) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("habitAdded", true)
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(
            route = Routes.Detail,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            HabitDetailScreen(
                habit = uiState.habits.firstOrNull { it.id == habitId },
                stats = uiState.stats[habitId],
                onBack = { navController.popBackStack() },
                onDelete = {
                    viewModel.deleteHabit(habitId) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("habitDeleted", true)
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(Routes.Settings) {
            val permissionDenied = stringResource(R.string.notification_permission_denied)
            val permissionSettings = stringResource(R.string.notification_permission_settings)
            val exactAlarmPermissionDenied = stringResource(R.string.exact_alarm_permission_denied)
            SettingsScreen(
                settings = uiState.reminderSettings,
                onBack = { navController.popBackStack() },
                onInfoClick = { navController.navigate(Routes.Info) },
                onNotificationsChange = { enabled ->
                    if (!enabled) {
                        viewModel.setNotificationsEnabled(false)
                    } else {
                        onNotificationPermissionRequest { granted, permanentlyDenied ->
                            if (granted) {
                                onExactAlarmPermissionRequest { exactGranted ->
                                    if (exactGranted) {
                                        viewModel.setNotificationsEnabled(true)
                                    } else {
                                        viewModel.setNotificationsEnabled(false)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(exactAlarmPermissionDenied)
                                        }
                                    }
                                }
                            } else {
                                viewModel.setNotificationsEnabled(false)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (permanentlyDenied) permissionSettings else permissionDenied
                                    )
                                }
                            }
                        }
                    }
                },
                onReminderTimeChange = { hour, minute ->
                    if (uiState.reminderSettings.notificationsEnabled) {
                        onExactAlarmPermissionRequest { exactGranted ->
                            if (exactGranted) {
                                viewModel.setReminderTime(hour, minute)
                            } else {
                                viewModel.setNotificationsEnabled(false)
                                scope.launch {
                                    snackbarHostState.showSnackbar(exactAlarmPermissionDenied)
                                }
                            }
                        }
                    } else {
                        onNotificationPermissionRequest { granted, permanentlyDenied ->
                            if (granted) {
                                onExactAlarmPermissionRequest { exactGranted ->
                                    if (exactGranted) {
                                        viewModel.enableNotificationsWithReminderTime(hour, minute)
                                    } else {
                                        viewModel.setNotificationsEnabled(false)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(exactAlarmPermissionDenied)
                                        }
                                    }
                                }
                            } else {
                                viewModel.setNotificationsEnabled(false)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (permanentlyDenied) permissionSettings else permissionDenied
                                    )
                                }
                            }
                        }
                    }
                },
                snackbarHostState = snackbarHostState
            )
        }
        composable(Routes.Info) {
            InfoScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
