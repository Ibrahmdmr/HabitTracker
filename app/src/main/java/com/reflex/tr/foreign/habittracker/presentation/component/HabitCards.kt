package com.reflex.tr.foreign.habittracker.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reflex.tr.foreign.habittracker.R
import com.reflex.tr.foreign.habittracker.data.model.Habit
import com.reflex.tr.foreign.habittracker.data.model.HabitType
import com.reflex.tr.foreign.habittracker.domain.usecase.HabitStats
import com.reflex.tr.foreign.habittracker.presentation.theme.NeonBlue
import com.reflex.tr.foreign.habittracker.presentation.theme.NeonGreen
import com.reflex.tr.foreign.habittracker.presentation.theme.NeonPurple
import com.reflex.tr.foreign.habittracker.presentation.theme.TextPrimary

val NeonGradient = Brush.linearGradient(
    colors = listOf(NeonPurple, NeonBlue),
    start = Offset.Zero,
    end = Offset(900f, 0f)
)

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    completed: Boolean = false,
    selected: Boolean = false,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    val borderColor = when {
        completed -> NeonGreen.copy(alpha = 0.48f)
        selected -> NeonBlue.copy(alpha = 0.52f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, cardShape)
            .border(1.dp, borderColor, cardShape)
            .clip(cardShape)
    ) {
        content()
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(),
        label = "gradientButtonScale"
    )
    Box(
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (enabled) 10f else 0f
                this.shape = shape
            }
            .clip(shape)
            .background(
                if (enabled) {
                    NeonGradient
                } else {
                    Brush.linearGradient(listOf(Color(0xFF334155).copy(alpha = 0.5f), Color(0xFF1E293B).copy(alpha = 0.42f)))
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) TextPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(NeonBlue.copy(alpha = 0.16f))
            .border(1.dp, NeonBlue.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeonBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MotivationCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NeonGradient, shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.motivation_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.motivation_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    today: String,
    stats: HabitStats?,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = habit.currentCount
    val isCompleted = progress >= habit.targetCount
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.01f else 1f,
        animationSpec = spring(),
        label = "habitScale"
    )

    NeonCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(onClick = onClick),
        completed = isCompleted
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) NeonGreen.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (isCompleted) NeonGreen.copy(alpha = 0.55f) else NeonBlue.copy(alpha = 0.22f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.emoji, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    Text(
                        text = stringResource(R.string.streak_days, stats?.currentStreak ?: 0),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) NeonGreen else NeonBlue
                    )
                    if (isCompleted) {
                        Text(
                            text = stringResource(R.string.completed),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
                if (habit.type == HabitType.CHECKBOX) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonGreen,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            if (habit.type == HabitType.COUNT) {
                NumberProgressControls(
                    progress = progress,
                    target = habit.targetCount,
                    unit = habit.unit,
                    onCountChange = onCountChange
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, NeonBlue.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.add_symbol),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonBlue
                )
            }
            Text(
                text = stringResource(R.string.empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            AccentButton(
                text = stringResource(R.string.add_habit_button),
                onClick = onAddHabitClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NeonPurple.copy(alpha = 0.65f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.pro),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    NeonCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 92.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NeonFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = modifier
            .height(44.dp)
            .widthIn(min = 96.dp)
            .graphicsLayer {
            shadowElevation = if (selected) 10f else 0f
            shape = RoundedCornerShape(999.dp)
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            selectedLabelColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outlineVariant
        ),
        leadingIcon = null,
        trailingIcon = null
    )
}

@Composable
fun neonTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = NeonBlue.copy(alpha = 0.72f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.82f),
    errorBorderColor = Color(0xFFFF6B6B),
    errorLabelColor = Color(0xFFFF8A8A),
    errorCursorColor = Color(0xFFFF8A8A),
    cursorColor = NeonBlue,
    focusedLabelColor = NeonBlue,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
)

private fun formatProgressValue(count: Int, unit: String): String {
    return if (unit == "adet") {
        count.toString()
    } else {
        "$count $unit"
    }
}

@Composable
private fun NumberProgressControls(
    progress: Int,
    target: Int,
    unit: String,
    onCountChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onCountChange((progress - 1).coerceAtLeast(0)) },
                enabled = progress > 0,
                border = BorderStroke(1.dp, NeonPurple),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(width = 56.dp, height = 44.dp)
            ) {
                Text(
                    stringResource(R.string.minus_symbol),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        R.string.progress_count,
                        "${formatProgressValue(progress, unit)} / ${formatProgressValue(target, unit)}"
                    ),
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = { onCountChange((progress + 1).coerceAtMost(target)) },
                enabled = progress < target,
                border = BorderStroke(1.dp, NeonBlue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(width = 56.dp, height = 44.dp)
            ) {
                Text(
                    stringResource(R.string.add_symbol),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        OutlinedButton(
            onClick = { onCountChange(target) },
            enabled = progress < target,
            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.72f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = NeonGreen.copy(alpha = 0.12f),
                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .height(40.dp)
                .align(Alignment.CenterHorizontally)
                .wrapContentWidth()
        ) {
            Text(
                stringResource(R.string.completed),
                color = if (progress < target) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
