package com.reflex.tr.foreign.habittracker.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reflex.tr.foreign.habittracker.R
import com.reflex.tr.foreign.habittracker.data.model.Habit
import com.reflex.tr.foreign.habittracker.domain.usecase.DayStatus
import com.reflex.tr.foreign.habittracker.domain.usecase.HabitStats
import com.reflex.tr.foreign.habittracker.presentation.component.NeonCard
import com.reflex.tr.foreign.habittracker.presentation.component.StatCard
import com.reflex.tr.foreign.habittracker.presentation.theme.NeonBlue
import com.reflex.tr.foreign.habittracker.presentation.theme.NeonGreen
import com.reflex.tr.foreign.habittracker.util.DateProvider

private fun formatTargetValue(count: Int, unit: String): String {
    return if (unit == "adet") {
        count.toString()
    } else {
        "$count $unit"
    }
}

private enum class DetailRange(val days: Int) {
    Seven(7),
    Thirty(30)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit?,
    stats: HabitStats?,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(DetailRange.Seven) }
    val dangerColor = Color(0xFFFF6B6B)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.habit_detail),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.back))
                    }
                },
                actions = {
                    if (habit != null) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text(
                                text = stringResource(R.string.delete),
                                color = dangerColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = NeonBlue,
                    actionIconContentColor = dangerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (habit == null || stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.habit_not_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            NeonCard(modifier = Modifier.fillMaxWidth(), completed = stats.currentStreak > 0) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = habit.emoji, style = MaterialTheme.typography.displaySmall)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        Text(
                            text = stringResource(
                                R.string.daily_target,
                                formatTargetValue(habit.targetCount, habit.unit)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    stringResource(R.string.current_streak),
                    stats.currentStreak.toString(),
                    Modifier
                        .weight(1f)
                        .height(96.dp)
                )
                StatCard(
                    stringResource(R.string.best_streak),
                    stats.bestStreak.toString(),
                    Modifier
                        .weight(1f)
                        .height(96.dp)
                )
            }
            StatCard(
                stringResource(R.string.total_days),
                stats.totalCompletedDays.toString(),
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            )
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val dateProvider = DateProvider()
                    val rangeDays = remember(habit, selectedRange) {
                        buildDayStatuses(habit, selectedRange.days, dateProvider)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.last_days),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        RangeSelector(
                            selectedRange = selectedRange,
                            onRangeSelected = { selectedRange = it }
                        )
                    }
                    DayStatusGrid(
                        days = rangeDays,
                        dateProvider = dateProvider,
                        columns = 7
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = stringResource(R.string.delete_habit_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.delete_habit_message))
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), color = NeonBlue)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = dangerColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun RangeSelector(
    selectedRange: DetailRange,
    onRangeSelected: (DetailRange) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RangeChip(
            text = stringResource(R.string.range_seven_days),
            selected = selectedRange == DetailRange.Seven,
            onClick = { onRangeSelected(DetailRange.Seven) }
        )
        RangeChip(
            text = stringResource(R.string.range_thirty_days),
            selected = selectedRange == DetailRange.Thirty,
            onClick = { onRangeSelected(DetailRange.Thirty) }
        )
    }
}

@Composable
private fun RangeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) NeonBlue.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.54f
                )
            )
            .border(
                1.dp,
                if (selected) NeonBlue.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outlineVariant,
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) NeonBlue else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayStatusGrid(
    days: List<DayStatus>,
    dateProvider: DateProvider,
    columns: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        days.chunked(columns).forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowDays.forEach { day ->
                    DayStatusItem(
                        day = day,
                        dateProvider = dateProvider,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowDays.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayStatusItem(
    day: DayStatus,
    dateProvider: DateProvider,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (day.isCompleted) NeonGreen.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (day.isCompleted) NeonGreen.copy(alpha = 0.55f) else NeonBlue.copy(alpha = 0.16f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (day.isCompleted) stringResource(R.string.checkmark) else "",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = dateProvider.shortLabel(day.date),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildDayStatuses(
    habit: Habit,
    days: Int,
    dateProvider: DateProvider
): List<DayStatus> {
    val completedDates = habit.completions
        .filterValues { it >= habit.targetCount }
        .keys
        .toSet()

    return (days - 1 downTo 0).map { daysAgo ->
        val date = dateProvider.dateDaysAgo(daysAgo)
        DayStatus(date = date, isCompleted = date in completedDates)
    }
}
