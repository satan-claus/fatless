package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.component.GhostButton
import com.niked.fatless.ui.component.IntervalCard
import com.niked.fatless.ui.component.TimerCard
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.WorkoutViewModel
import com.niked.fatless.utils.formatDuration

@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workout = uiState.workout ?: return

    // 1. Расчеты времени для мета-данных
    val totalWorkoutTime = workout.intervals.sumOf { it.seconds }

    // Текущее накопленное время (прошедшее)
    val elapsedSeconds = totalWorkoutTime - uiState.timeLeft

    val totalTimeStr = formatDuration(totalWorkoutTime)

    // 2. Мета-данные для TopBar
    val topBarMeta = when (uiState.status) {
        is WorkoutState.READY -> totalTimeStr
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

    // 3. Логика отображения таймера
    val timeToShow = when (uiState.status) {
        is WorkoutState.READY -> totalWorkoutTime
        is WorkoutState.COMPLETED -> 0
        else -> uiState.timeLeft
    }

    val timerSubText = when (uiState.status) {
        is WorkoutState.READY -> "Общее время"
        is WorkoutState.COMPLETED -> "Прошло ${formatDuration(totalWorkoutTime)} из $totalTimeStr"
        else -> "Прошло ${formatDuration(elapsedSeconds)} из $totalTimeStr"
    }

    // Расчет общего прогресса (от 0.0 до 1.0)
    val totalProgress = (elapsedSeconds.toFloat() / totalWorkoutTime.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
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
            currentIntervalName = workout.intervals.getOrNull(uiState.currentIntervalIndex)?.name ?: "Приготовьтесь",
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

        // 4. Список интервалов
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(workout.intervals) { index, interval ->
                // Прогресс внутри текущего интервала
                val intervalProgress = if (index == uiState.currentIntervalIndex && uiState.status is WorkoutState.RUNNING) {
                    val currentIntSeconds = interval.seconds.toFloat()
                    if (currentIntSeconds > 0) (currentIntSeconds - uiState.timeLeft) / currentIntSeconds else 0f
                } else 0f

                IntervalCard(
                    interval = interval,
                    index = index + 1,
                    isActive = index == uiState.currentIntervalIndex,
                    isPaused = uiState.status is WorkoutState.PAUSED,
                    isCompleted = index < uiState.currentIntervalIndex,
                    isFinalDone = uiState.status is WorkoutState.COMPLETED,
                    progress = intervalProgress
                )
            }
        }

        // 5. Подвал управления
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
                        onClick = { /* Нужно добавить метод resetWorkout во ViewModel */ }
                    )
                }
                is WorkoutState.COMPLETED -> {
                    Button(
                        onClick = { onBackClick() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppSecondary)
                    ) {
                        Text("В МЕНЮ", style = AppTypography.labelMedium)
                    }
                }
            }
        }
    }
}
