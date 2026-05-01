package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.viewmodel.WorkoutViewModel
import com.niked.fatless.core.utils.formatDuration
import com.niked.fatless.ui.component.GhostButton
import com.niked.fatless.ui.component.IntervalCard
import com.niked.fatless.ui.component.TimerCard
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workout = uiState.workout ?: return

    // 1. ВРЕМЕННЫЕ РАСЧЕТЫ
    val totalWorkoutTime = workout.intervals.sumOf { it.seconds }
    val completedIntervalsTime =
        workout.intervals.take(uiState.currentIntervalIndex).sumOf { it.seconds }
    val currentInterval = workout.intervals.getOrNull(uiState.currentIntervalIndex)
    val timePassedInCurrent = (currentInterval?.seconds ?: 0) - uiState.timeLeft
    val elapsedSeconds = completedIntervalsTime + timePassedInCurrent

    // 2. ПРОГРЕСС И ТЕКСТЫ
    val totalProgress =
        if (totalWorkoutTime > 0) elapsedSeconds.toFloat() / totalWorkoutTime.toFloat() else 0f

    val timeToShow = when (uiState.status) {
        is WorkoutState.READY -> totalWorkoutTime
        is WorkoutState.COMPLETED -> 0
        else -> uiState.timeLeft
    }

    val timerSubText = when (uiState.status) {
        is WorkoutState.READY -> stringResource(R.string.workout_status_ready_sub)
        else -> stringResource(
            R.string.workout_status_progress,
            formatDuration(elapsedSeconds),
            formatDuration(totalWorkoutTime)
        )
    }

    // 3. МЕТА-ДАННЫЕ ДЛЯ TOPBAR (Текст справа в заголовке)
    val topBarMeta = when (uiState.status) {
        is WorkoutState.READY -> formatDuration(totalWorkoutTime)
        is WorkoutState.RUNNING -> stringResource(
            R.string.workout_status_running_meta,
            formatDuration(elapsedSeconds)
        )
        is WorkoutState.PAUSED -> "❚❚ " + stringResource(R.string.workout_status_paused)
        is WorkoutState.COMPLETED -> stringResource(R.string.workout_status_completed)
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
            .navigationBarsPadding()
    ) {
        // 1. ТОПБАР
        WorkoutTopBar(
            title = workout.title,
            subTitle = topBarMeta,
            subTitleColor = topBarMetaColor,
            onBackClick = onBackClick
        )

        // 2. КОНТЕНТ
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            TimerCard(
                state = uiState.status,
                currentIntervalName = currentInterval?.name ?: stringResource(R.string.workout_ready_hint),
                displayTime = formatDuration(timeToShow),
                totalProgress = totalProgress,
                subText = timerSubText
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.workout_section_intervals),
                style = AppTypography.titleMedium,
                color = AppTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
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
        }

        // 3. ПОДВАЛ УПРАВЛЕНИЯ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
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
                        Text(stringResource(R.string.workout_btn_start), style = AppTypography.labelMedium)
                    }
                }

                is WorkoutState.RUNNING -> {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppOrange)
                    ) {
                        Text(stringResource(R.string.workout_btn_pause), style = AppTypography.labelMedium)
                    }
                    Button(
                        onClick = { viewModel.nextInterval() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Text(stringResource(R.string.workout_btn_next), style = AppTypography.labelMedium)
                    }
                }

                is WorkoutState.PAUSED -> {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Text(stringResource(R.string.workout_btn_resume), style = AppTypography.labelMedium)
                    }
                    GhostButton(
                        text = stringResource(R.string.workout_btn_reset),
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
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.workout_btn_restart), style = AppTypography.labelMedium)
                        }
                    }
                    GhostButton(
                        text = stringResource(R.string.workout_btn_to_menu),
                        onClick = onBackClick
                    )
                }
            }
        }
    }
}
