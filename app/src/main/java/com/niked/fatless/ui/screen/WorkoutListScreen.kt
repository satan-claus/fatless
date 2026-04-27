package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.component.WorkoutRow
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppError
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.viewmodel.WorkoutListUiState
import com.niked.fatless.ui.viewmodel.WorkoutListViewModel

@Composable
fun WorkoutListScreen(
    onWorkoutClick: (String) -> Unit,
    onAddWorkoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit,
    viewModel: WorkoutListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        // Верхняя панель (subTitle меняется в зависимости от стейта)
        WorkoutTopBar(
            title = "Мои тренировки",
            subTitle = if (state is WorkoutListUiState.Success) {
                "${(state as WorkoutListUiState.Success).workouts.size} наборов"
            } else "",
            onBackClick = onExitClick,
            actions = {
                IconButton(onClick = { onSettingsClick() }) {
                    // Используем стандартную шестеренку
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = AppTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Основной контент
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            when (val s = state) {
                is WorkoutListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppPrimary
                    )
                }
                is WorkoutListUiState.Success -> {
                    if (s.workouts.isEmpty()) {
                        Text(
                            text = "У вас пока нет тренировок",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.workouts) { workout ->
                                WorkoutRow(
                                    workout = workout,
                                    onClick = { onWorkoutClick(workout.id) }
                                )
                            }
                        }
                    }
                }
                is WorkoutListUiState.Error -> {
                    Text(
                        text = s.message,
                        color = AppError,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Кнопка добавления
        Button(
            onClick = onAddWorkoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(vertical = 24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
        ) {
            Text("+ Добавить тренировку", style = MaterialTheme.typography.labelMedium)
        }
    }
}
