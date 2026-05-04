package com.reflex.tr.foreign.habittracker.presentation.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reflex.tr.foreign.habittracker.data.local.HabitLocalDataSource
import com.reflex.tr.foreign.habittracker.data.local.SettingsLocalDataSource
import com.reflex.tr.foreign.habittracker.data.model.HabitType
import com.reflex.tr.foreign.habittracker.data.repository.HabitRepository
import com.reflex.tr.foreign.habittracker.data.repository.SettingsRepository
import com.reflex.tr.foreign.habittracker.domain.usecase.HabitStatsUseCase
import com.reflex.tr.foreign.habittracker.util.DateProvider
import com.reflex.tr.foreign.habittracker.util.ReminderReceiver
import com.reflex.tr.foreign.habittracker.util.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val dateProvider = DateProvider()
    private val statsUseCase = HabitStatsUseCase(dateProvider)
    private val repository = HabitRepository(HabitLocalDataSource(application))
    private val settingsRepository = SettingsRepository(SettingsLocalDataSource(application))
    private val reminderScheduler = ReminderScheduler(application)

    init {
        ReminderReceiver.ensureChannel(application)
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            if (settings.notificationsEnabled) {
                reminderScheduler.schedule(settings)
            }
        }
    }

    val uiState = combine(repository.habits, settingsRepository.settings) { habits, settings ->
            val today = dateProvider.today()
            val todayHabits = habits.map { habit ->
                habit.copy(currentCount = habit.completions[today] ?: 0)
            }
            HabitUiState(
                habits = todayHabits,
                stats = todayHabits.associate { it.id to statsUseCase.statsFor(it) },
                today = today,
                reminderSettings = settings
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitUiState(today = dateProvider.today())
        )

    fun addHabit(name: String, emoji: String, type: HabitType, targetCount: Int, unit: String, onSaved: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addHabit(name, emoji, type, targetCount, unit)
            onSaved()
        }
    }

    fun deleteHabit(id: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteHabit(id)
            onDeleted()
        }
    }

    fun toggleToday(id: Long) {
        val state = uiState.value
        val habit = state.habits.firstOrNull { it.id == id } ?: return
        if (habit.type != HabitType.CHECKBOX) return
        val todayCount = habit.currentCount
        val nextCount = if (todayCount >= 1) 0 else 1
        viewModelScope.launch {
            repository.setTodayProgress(id, state.today, nextCount)
        }
    }

    fun changeTodayCount(id: Long, count: Int) {
        val state = uiState.value
        val habit = state.habits.firstOrNull { it.id == id } ?: return
        if (habit.type != HabitType.COUNT) return
        viewModelScope.launch {
            repository.setTodayProgress(id, state.today, count)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            val settings = settingsRepository.settings.first()
            reminderScheduler.schedule(settings)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setReminderTime(hour, minute)
            val settings = settingsRepository.settings.first()
            reminderScheduler.schedule(settings)
        }
    }

    fun enableNotificationsWithReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(true)
            settingsRepository.setReminderTime(hour, minute)
            val settings = settingsRepository.settings.first()
            reminderScheduler.schedule(settings)
        }
    }
}
