package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.component.*
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.WorkoutViewModel
import com.niked.fatless.core.utils.formatDuration

@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workout = uiState.workout ?: return

    // 1. ВРЕМЕННЫЕ РАСЧЕТЫ
    val totalWorkoutTime = workout.intervals.sumOf { it.seconds }
    val completedIntervalsTime = workout.intervals.take(uiState.currentIntervalIndex).sumOf { it.seconds }
    val currentInterval = workout.intervals.getOrNull(uiState.currentIntervalIndex)
    val timePassedInCurrent = (currentInterval?.seconds ?: 0) - uiState.timeLeft
    val elapsedSeconds = completedIntervalsTime + timePassedInCurrent

    // 2. ПРОГРЕСС И ТЕКСТЫ
    val totalProgress = if (totalWorkoutTime > 0) elapsedSeconds.toFloat() / totalWorkoutTime.toFloat() else 0f

    val timeToShow = when (uiState.status) {
        is WorkoutState.READY -> totalWorkoutTime
        is WorkoutState.COMPLETED -> 0
        else -> uiState.timeLeft
    }

    val timerSubText = when (uiState.status) {
        is WorkoutState.READY -> "Общее время"
        else -> "Прошло ${formatDuration(elapsedSeconds)} из ${formatDuration(totalWorkoutTime)}"
    }

    // 3. МЕТА-ДАННЫЕ ДЛЯ TOPBAR
    val topBarMeta = when (uiState.status) {
        is WorkoutState.READY -> formatDuration(totalWorkoutTime)
        is WorkoutState.RUNNING -> "● ${formatDuration(elapsedSeconds)}"
        is WorkoutState.PAUSED -> "❚❚ ПАУЗА"
        is WorkoutState.COMPLETED -> "ЗАВЕРШЕНА"
    }

    val topBarMetaColor = when (uiState.status) {
        is WorkoutState.READY -> AppTextSecondary
        is WorkoutState.RUNNING -> AppPrimary
        is WorkoutState.PAUSED -> AppOrange
        is WorkoutState.COMPLETED -> AppSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        WorkoutTopBar(
            title = workout.title,
            subTitle = topBarMeta,
            subTitleColor = topBarMetaColor,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        TimerCard(
            state = uiState.status,
            currentIntervalName = currentInterval?.name ?: "Приготовьтесь",
            displayTime = formatDuration(timeToShow),
            totalProgress = totalProgress,
            subText = timerSubText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Интервалы",
            style = AppTypography.titleMedium,
            color = AppTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. СПИСОК ИНТЕРВАЛОВ
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(workout.intervals) { index, interval ->
                val intervalProgress = if (index == uiState.currentIntervalIndex) {
                    val total = interval.seconds.toFloat()
                    if (total > 0) (total - uiState.timeLeft) / total else 0f
                } else 0f

                IntervalCard(
                    interval = interval,
                    index = index + 1,
                    isActive = index == uiState.currentIntervalIndex,
                    isCompleted = index < uiState.currentIntervalIndex,
                    state = uiState.status,
                    progress = intervalProgress,
                    stateSteps = uiState.currentIntervalSteps,
                    onClick = { viewModel.nextInterval() }
                )
            }
        }

        // 5. ПОДВАЛ УПРАВЛЕНИЯ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (uiState.status) {
                is WorkoutState.READY -> {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Text("СТАРТ", style = AppTypography.labelMedium)
                    }
                }

                is WorkoutState.RUNNING -> {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppOrange)
                    ) {
                        Text("ПАУЗА", style = AppTypography.labelMedium)
                    }
                    Button(
                        onClick = { viewModel.nextInterval() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Text("ГОТОВО / СЛЕДУЮЩИЙ", style = AppTypography.labelMedium)
                    }
                }

                is WorkoutState.PAUSED -> {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Text("ПРОДОЛЖИТЬ", style = AppTypography.labelMedium)
                    }
                    GhostButton(
                        text = "СБРОСИТЬ",
                        onClick = { viewModel.resetWorkout() }
                    )
                }

                is WorkoutState.COMPLETED -> {
                    Button(
                        onClick = {
                            viewModel.resetWorkout()
                            viewModel.toggleTimer()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppSecondary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ЗАПУСТИТЬ ЗАНОВО", style = AppTypography.labelMedium)
                        }
                    }
                    GhostButton(
                        text = "В МЕНЮ",
                        onClick = onBackClick
                    )
                }
            }
        }
    }
}
