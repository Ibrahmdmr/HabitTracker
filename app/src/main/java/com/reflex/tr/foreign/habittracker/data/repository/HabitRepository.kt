package com.reflex.tr.foreign.habittracker.data.repository

import com.reflex.tr.foreign.habittracker.data.local.HabitLocalDataSource
import com.reflex.tr.foreign.habittracker.data.model.Habit
import com.reflex.tr.foreign.habittracker.data.model.HabitType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HabitRepository(
    private val localDataSource: HabitLocalDataSource
) {
    val habits: Flow<List<Habit>> = localDataSource.habits

    suspend fun addHabit(name: String, emoji: String, type: HabitType, targetCount: Int, unit: String) {
        val current = localDataSource.habits.first()
        val dailyTarget = if (type == HabitType.CHECKBOX) 1 else targetCount.coerceIn(1, 100_000)
        val habit = Habit(
            id = System.currentTimeMillis(),
            name = name.trim(),
            emoji = emoji.ifBlank { "✓" }.trim(),
            type = type,
            targetCount = dailyTarget,
            unit = if (type == HabitType.CHECKBOX) "adet" else unit.ifBlank { "adet" },
            currentCount = 0
        )
        localDataSource.saveHabits(current + habit)
    }

    suspend fun deleteHabit(id: Long) {
        localDataSource.saveHabits(localDataSource.habits.first().filterNot { it.id == id })
    }

    suspend fun setTodayProgress(id: Long, date: String, count: Int) {
        val updated = localDataSource.habits.first().map { habit ->
            if (habit.id != id) {
                habit
            } else {
                val nextCompletions = habit.completions.toMutableMap()
                val target = if (habit.type == HabitType.CHECKBOX) 1 else habit.targetCount
                val nextCount = count.coerceIn(0, target)
                if (count <= 0) {
                    nextCompletions.remove(date)
                } else {
                    nextCompletions[date] = nextCount
                }
                habit.copy(
                    currentCount = nextCompletions[date] ?: 0,
                    completions = nextCompletions
                )
            }
        }
        localDataSource.saveHabits(updated)
    }
}
