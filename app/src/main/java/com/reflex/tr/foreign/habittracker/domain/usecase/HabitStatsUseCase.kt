package com.reflex.tr.foreign.habittracker.domain.usecase

import com.reflex.tr.foreign.habittracker.data.model.Habit
import com.reflex.tr.foreign.habittracker.util.DateProvider

data class HabitStats(
    val totalCompletedDays: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val lastSevenDays: List<DayStatus>
)

data class DayStatus(
    val date: String,
    val isCompleted: Boolean
)

class HabitStatsUseCase(
    private val dateProvider: DateProvider = DateProvider()
) {
    fun statsFor(habit: Habit): HabitStats {
        val completedDates = habit.completions
            .filterValues { it >= habit.targetCount }
            .keys
            .toSet()

        return HabitStats(
            totalCompletedDays = completedDates.size,
            currentStreak = currentStreak(completedDates),
            bestStreak = bestStreak(completedDates),
            lastSevenDays = (6 downTo 0).map { daysAgo ->
                val date = dateProvider.dateDaysAgo(daysAgo)
                DayStatus(date = date, isCompleted = date in completedDates)
            }
        )
    }

    private fun currentStreak(completedDates: Set<String>): Int {
        var streak = 0
        var daysAgo = 0
        while (dateProvider.dateDaysAgo(daysAgo) in completedDates) {
            streak++
            daysAgo++
        }
        return streak
    }

    private fun bestStreak(completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sortedDates = completedDates.sorted()
        var best = 1
        var current = 1
        sortedDates.zipWithNext { previous, next ->
            if (dateProvider.nextDate(previous) == next) {
                current++
            } else {
                current = 1
            }
            best = maxOf(best, current)
        }
        return best
    }
}
