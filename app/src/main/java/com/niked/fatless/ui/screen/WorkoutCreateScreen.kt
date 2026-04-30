package com.niked.fatless.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.EditableIntervalItem
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.WorkoutCreateViewModel

@Composable
fun WorkoutCreateScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutCreateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        WorkoutTopBar(
            title = "Новая тренировка",
            subTitle = if (state.title.isBlank()) "Конструктор" else state.title,
            onBackClick = onBackClick,
            actions = {
                IconButton(
                    onClick = { viewModel.saveWorkout { onBackClick() } },
                    enabled = state.title.isNotBlank() && !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = ColorSteps
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (state.title.isNotBlank()) AppPrimary else AppTextTertiary
                        )
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f) // Занимает всё место до низа
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "НАЗВАНИЕ",
                    style = AppTypography.titleSmall,
                    color = AppTextTertiary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Например: Утренняя пахота", style = AppTypography.bodyLarge)
                    },
                    textStyle = AppTypography.bodyLarge.copy(color = AppTextPrimary),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppSurface,
                        unfocusedContainerColor = AppSurface,
                        focusedBorderColor = AppPrimary,
                        unfocusedBorderColor = AppBorder,
                        cursorColor = AppPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ИНТЕРВАЛЫ",
                    style = AppTypography.titleSmall,
                    color = AppTextTertiary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            itemsIndexed(state.intervals) { index, interval ->
                EditableIntervalItem(
                    index = index,
                    interval = interval,
                    onNameChange = { name ->
                        viewModel.updateInterval(index, name, interval.seconds, interval.reps, interval.trackSteps)
                    },
                    onSecondsChange = { secs ->
                        viewModel.updateInterval(index, interval.name, secs, interval.reps, interval.trackSteps)
                    },
                    onRepsChange = { newReps ->
                        viewModel.updateInterval(index, interval.name, interval.seconds, newReps, interval.trackSteps)
                    },
                    onTrackStepsChange = { isEnabled ->
                        viewModel.updateInterval(index, interval.name, interval.seconds, interval.reps, isEnabled)
                    },
                    onRemove = { viewModel.removeInterval(index) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addInterval() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, AppPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ДОБАВИТЬ ИНТЕРВАЛ", style = AppTypography.labelMedium)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

