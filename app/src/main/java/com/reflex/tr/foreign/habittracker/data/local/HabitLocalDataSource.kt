package com.reflex.tr.foreign.habittracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reflex.tr.foreign.habittracker.data.model.Habit
import com.reflex.tr.foreign.habittracker.data.model.HabitType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.habitDataStore by preferencesDataStore(name = "habit_tracker")

class HabitLocalDataSource(context: Context) {
    private val dataStore = context.habitDataStore
    private val habitsKey = stringPreferencesKey("habits")

    val habits: Flow<List<Habit>> = dataStore.data.map { preferences ->
        decodeHabits(preferences[habitsKey].orEmpty())
    }

    suspend fun saveHabits(habits: List<Habit>) {
        dataStore.edit { preferences ->
            preferences[habitsKey] = encodeHabits(habits)
        }
    }

    private fun encodeHabits(habits: List<Habit>): String {
        val array = JSONArray()
        habits.forEach { habit ->
            val completions = JSONObject()
            habit.completions.forEach { (date, count) ->
                completions.put(date, count)
            }
            array.put(
                JSONObject()
                    .put("id", habit.id)
                    .put("name", habit.name)
                    .put("emoji", habit.emoji)
                    .put("type", habit.type.name)
                    .put("targetCount", habit.targetCount)
                    .put("unit", habit.unit)
                    .put("currentCount", habit.currentCount)
                    .put("completions", completions)
            )
        }
        return array.toString()
    }

    private fun decodeHabits(value: String): List<Habit> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(value)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val type = decodeHabitType(item)
                    val targetCount = item.optInt(
                        "targetCount",
                        item.optInt("dailyTarget", 1)
                    ).coerceIn(1, 100_000)
                    val completionsObject = item.optJSONObject("completions") ?: JSONObject()
                    val completions = buildMap {
                        val keys = completionsObject.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            put(key, completionsObject.optInt(key, 0))
                        }
                    }
                    add(
                        Habit(
                            id = item.getLong("id"),
                            name = item.getString("name"),
                            emoji = item.optString("emoji", "✓"),
                            type = type,
                            targetCount = if (type == HabitType.CHECKBOX) 1 else targetCount,
                            unit = item.optString("unit", "adet"),
                            currentCount = item.optInt("currentCount", 0).coerceAtLeast(0),
                            completions = completions
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun decodeHabitType(item: JSONObject): HabitType {
        val rawType = item.optString(
            "type",
            item.optString("targetType", HabitType.CHECKBOX.name)
        )
        return when (rawType) {
            "Checkbox", "CHECKBOX" -> HabitType.CHECKBOX
            "Number", "COUNT" -> HabitType.COUNT
            else -> HabitType.CHECKBOX
        }
    }
}
