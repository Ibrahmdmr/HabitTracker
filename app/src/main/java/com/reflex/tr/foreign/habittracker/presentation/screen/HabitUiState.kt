package com.reflex.tr.foreign.habittracker.presentation.screen

import com.reflex.tr.foreign.habittracker.data.model.Habit
import com.reflex.tr.foreign.habittracker.data.model.ReminderSettings
import com.reflex.tr.foreign.habittracker.domain.usecase.HabitStats

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val stats: Map<Long, HabitStats> = emptyMap(),
    val today: String = "",
    val reminderSettings: ReminderSettings = ReminderSettings()
)
