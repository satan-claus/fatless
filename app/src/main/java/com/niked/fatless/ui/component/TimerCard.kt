package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary

@Composable
fun TimerCard(
    state: WorkoutState,
    currentIntervalName: String,
    displayTime: String,
    totalProgress: Float,
    subText: String,
    modifier: Modifier = Modifier
) {
    // Логика цветов на основе состояния
    val borderColor = when (state) {
        is WorkoutState.READY -> AppBorder
        is WorkoutState.RUNNING -> AppPrimary
        is WorkoutState.PAUSED -> AppOrange
        is WorkoutState.COMPLETED -> AppSecondary
    }

    val stateColor = when(state) {
        is WorkoutState.RUNNING -> AppPrimary
        is WorkoutState.PAUSED -> AppOrange
        is WorkoutState.COMPLETED -> AppSecondary
        else -> AppTextTertiary
    }

    val workoutNameColor = when(state) {
        is WorkoutState.READY -> AppTextSecondary
        is WorkoutState.COMPLETED -> AppSecondary
        else -> AppTextTertiary
    }

    val timerColor = when (state) {
        is WorkoutState.RUNNING -> AppPrimary
        is WorkoutState.PAUSED -> AppOrange
        is WorkoutState.COMPLETED -> AppSecondary
        else -> AppTextPrimary
    }

    // Градиент фона
    val bgGradient = when (state) {
        is WorkoutState.RUNNING -> Brush.verticalGradient(listOf(AppPrimary.copy(alpha = 0.04f), AppSurface))
        is WorkoutState.PAUSED -> Brush.verticalGradient(listOf(AppOrange.copy(alpha = 0.04f), AppSurface))
        is WorkoutState.COMPLETED -> Brush.verticalGradient(listOf(AppSecondary.copy(alpha = 0.04f), AppSurface))
        else -> Brush.verticalGradient(listOf(AppSurface, AppSurface))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppSurface,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(bgGradient)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Метка состояния
            Text(
                text = when(state) {
                    is WorkoutState.READY -> stringResource(R.string.workout_timer_state_ready)
                    is WorkoutState.RUNNING -> stringResource(R.string.workout_timer_state_running)
                    is WorkoutState.PAUSED -> stringResource(R.string.workout_timer_state_paused)
                    is WorkoutState.COMPLETED -> stringResource(R.string.workout_timer_state_completed)
                },
                style = AppTypography.titleSmall,
                color = stateColor
            )

            // Название текущего интервала или похвала
            Text(
                text = if (state is WorkoutState.COMPLETED)
                    stringResource(R.string.workout_timer_hint_completed)
                else
                    currentIntervalName,
                style = AppTypography.titleMedium,
                color = workoutNameColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // ТАЙМЕР
            Text(
                text = displayTime,
                style = AppTypography.displayLarge,
                modifier = Modifier.padding(vertical = 12.dp),
                color = timerColor
            )

            // Подзаголовок (Общее время / Прогресс)
            // subText приходит уже анимированным и с ресурсами из WorkoutScreen
            Text(
                text = subText,
                style = AppTypography.bodySmall,
                color = AppTextTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Полоска прогресса
            LinearProgressIndicator(
                progress = { totalProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = stateColor,
                trackColor = AppBorder,
                strokeCap = StrokeCap.Round
            )
        }
    }
}