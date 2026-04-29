package com.reflex.tr.foreign.habittracker.data.model

data class Habit(
    val id: Long,
    val name: String,
    val emoji: String,
    val type: HabitType,
    val targetCount: Int,
    val unit: String = "adet",
    val currentCount: Int = 0,
    val completions: Map<String, Int> = emptyMap()
)

enum class HabitType {
    CHECKBOX,
    COUNT
}
