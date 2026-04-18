package com.niked.fatless.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.EditableIntervalRow
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.WorkoutCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCreateScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutCreateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Новая тренировка",
                        style = AppTypography.titleMedium // Используем нашу Roboto 16 SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = AppTextSecondary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveWorkout { onBackClick() } },
                        enabled = state.title.isNotBlank() && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = AppPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (state.title.isNotBlank()) AppPrimary else AppTextTertiary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBackground
                )
            )
        },
        containerColor = AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "НАЗВАНИЕ",
                    style = AppTypography.titleSmall, // Наш Roboto 11 Bold CAPS
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
                        disabledContainerColor = AppSurface,
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
                EditableIntervalRow(
                    index = index,
                    interval = interval,
                    onNameChange = { name ->
                        // Передаем текущие секунды и текущие репсы
                        viewModel.updateInterval(index, name, interval.seconds, interval.reps)
                    },
                    onSecondsChange = { secs ->
                        // Передаем новое время и текущие репсы
                        viewModel.updateInterval(index, interval.name, secs, interval.reps)
                    },
                    onRepsChange = { newReps ->
                        // Передаем текущее время и новые репсы
                        viewModel.updateInterval(index, interval.name, interval.seconds, newReps)
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ДОБАВИТЬ ИНТЕРВАЛ",
                        style = AppTypography.labelMedium // Roboto 15 SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
