package com.reflex.tr.foreign.habittracker.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateProvider {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("tr-TR"))

    fun today(): String = LocalDate.now().format(formatter)

    fun displayDate(date: String): String {
        return runCatching {
            LocalDate.parse(date, formatter).format(displayFormatter)
        }.getOrDefault(date)
    }

    fun dateDaysAgo(daysAgo: Int): String {
        return LocalDate.now().minusDays(daysAgo.toLong()).format(formatter)
    }

    fun nextDate(date: String): String {
        return runCatching {
            LocalDate.parse(date, formatter).plusDays(1).format(formatter)
        }.getOrDefault(date)
    }

    fun shortLabel(date: String): String = date.takeLast(5)
}
