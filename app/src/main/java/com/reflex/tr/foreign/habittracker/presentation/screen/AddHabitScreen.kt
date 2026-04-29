package com.reflex.tr.foreign.habittracker.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import com.reflex.tr.foreign.habittracker.R
import com.reflex.tr.foreign.habittracker.data.model.HabitType
import com.reflex.tr.foreign.habittracker.presentation.component.GradientButton
import com.reflex.tr.foreign.habittracker.presentation.component.NeonCard
import com.reflex.tr.foreign.habittracker.presentation.component.NeonFilterChip
import com.reflex.tr.foreign.habittracker.presentation.component.neonTextFieldColors
import com.reflex.tr.foreign.habittracker.presentation.theme.NeonBlue

private fun formatTargetValue(count: Int, unit: String): String {
    return if (unit == "adet") {
        count.toString()
    } else {
        "$count $unit"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onSave: (String, String, HabitType, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💧") }
    var type by remember { mutableStateOf(HabitType.CHECKBOX) }
    var targetCountInput by remember { mutableStateOf("5") }
    val defaultUnit = stringResource(R.string.unit_piece)
    var unit by remember { mutableStateOf(defaultUnit) }
    val parsedTargetCount = targetCountInput.toIntOrNull()
    val isNumberTargetValid = targetCountInput.isNotBlank() &&
        targetCountInput.all { it.isDigit() } &&
        parsedTargetCount != null &&
        parsedTargetCount in 1..100_000
    val displayedTargetCount = parsedTargetCount?.takeIf { isNumberTargetValid } ?: 5
    val targetCount = if (type == HabitType.CHECKBOX) 1 else displayedTargetCount
    val canSave = name.isNotBlank() && (type == HabitType.CHECKBOX || isNumberTargetValid)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    fun clearInputFocus() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_habit), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = NeonBlue,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::clearInputFocus
                )
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.habit_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.habit_name)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { clearInputFocus() }
                            ),
                            colors = neonTextFieldColors()
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.emoji),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        EmojiPicker(selectedEmoji = emoji, onEmojiSelected = { emoji = it })
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.daily_goal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeonFilterChip(
                                selected = type == HabitType.CHECKBOX,
                                onClick = { type = HabitType.CHECKBOX },
                                text = stringResource(R.string.checkbox_goal),
                                modifier = Modifier.weight(1f)
                            )
                            NeonFilterChip(
                                selected = type == HabitType.COUNT,
                                onClick = { type = HabitType.COUNT },
                                text = stringResource(R.string.number_goal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (type == HabitType.COUNT) {
                            NeonCard(
                                modifier = Modifier.fillMaxWidth(),
                                selected = true
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.target_count, formatTargetValue(displayedTargetCount, unit)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    OutlinedTextField(
                                        value = targetCountInput,
                                        onValueChange = { value ->
                                            val nextInput = value.filter { it.isDigit() }.take(6)
                                            targetCountInput = nextInput
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(stringResource(R.string.daily_target_count_input)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { clearInputFocus() }
                                        ),
                                        isError = targetCountInput.isBlank() || !isNumberTargetValid,
                                        supportingText = {
                                            if (targetCountInput.isBlank() || !isNumberTargetValid) {
                                                Text(stringResource(R.string.target_count_error))
                                            }
                                        },
                                        colors = neonTextFieldColors()
                                    )
                                    Text(
                                        text = stringResource(R.string.unit_label),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    UnitPicker(selectedUnit = unit, onUnitSelected = { unit = it })
                                }
                            }
                        }
                    }
                }
            }
            GradientButton(
                text = stringResource(R.string.save),
                onClick = {
                    clearInputFocus()
                    onSave(name, emoji, type, targetCount, unit)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                enabled = canSave
            )
        }
    }
}

@Composable
private fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf("💧", "🏃", "📚", "🧘", "🥗", "💪", "🚶", "💤", "🧠", "📝", "☕", "🍎")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
    ) {
        items(emojis) { item ->
            EmojiChip(
                selected = selectedEmoji == item,
                onClick = { onEmojiSelected(item) },
                emoji = item
            )
        }
    }
}

@Composable
private fun UnitPicker(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit
) {
    val units = listOf(
        stringResource(R.string.unit_piece),
        stringResource(R.string.unit_glass),
        stringResource(R.string.unit_page),
        stringResource(R.string.unit_minute),
        stringResource(R.string.unit_step)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
    ) {
        items(units) { item ->
            NeonFilterChip(
                selected = selectedUnit == item,
                onClick = { onUnitSelected(item) },
                text = item,
                modifier = Modifier.height(44.dp)
            )
        }
    }
}

@Composable
private fun EmojiChip(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            selected -> 1.1f
            else -> 1f
        },
        animationSpec = spring(),
        label = "emojiScale"
    )
    val shape = CircleShape

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) NeonBlue else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
    }
}
