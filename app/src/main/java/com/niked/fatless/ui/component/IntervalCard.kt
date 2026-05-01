package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.R
import com.niked.fatless.core.utils.formatDuration
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppOrangeLight
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppPrimaryLight
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun IntervalCard(
    interval: Interval,
    index: Int,
    isActive: Boolean,
    isCompleted: Boolean,
    state: WorkoutState,
    progress: Float,
    stateSteps: Int,
    onClick: () -> Unit = {}
) {
    val isPaused = state is WorkoutState.PAUSED
    val isAllDone = state is WorkoutState.COMPLETED

    val showAsActive = isActive && !isAllDone
    val activeColor = if (isPaused) AppOrange else AppPrimary
    val activeLightColor = if (isPaused) AppOrangeLight else AppPrimaryLight
    val containerAlpha = if (isCompleted || isAllDone) 0.55f else 1f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(containerAlpha)
            .clickable(enabled = showAsActive) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = AppSurface,
        border = BorderStroke(1.5.dp, if (showAsActive) activeColor else Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (showAsActive && progress > 0f) {
                        drawRect(
                            color = activeLightColor,
                            size = size.copy(width = size.width * progress)
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Кругляшок слева (Номер или Галка)
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = if (showAsActive) activeColor else AppBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted || isAllDone) {
                            val checkMarkColor = if (isAllDone) AppSecondary else AppTextTertiary
                            Text(
                                text = "✓",
                                color = if (showAsActive) Color.White else checkMarkColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = index.toString(),
                                style = AppTypography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (showAsActive) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (showAsActive) Color.White else AppTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Название + Цель (Повторы) + ШАГИ
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = interval.name,
                        style = AppTypography.labelLarge,
                        color = AppTextPrimary,
                        textDecoration = if (isCompleted || isAllDone) TextDecoration.LineThrough else null
                    )

                    // СИЛОВАЯ ЦЕЛЬ (Повторы)
                    if (interval.reps != null && interval.reps > 0) {
                        Text(
                            text = stringResource(R.string.workout_interval_card_reps_goal, interval.reps),
                            style = AppTypography.bodySmall,
                            color = if (showAsActive) activeColor else AppOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // КАРДИО ЦЕЛЬ (Шаги в реальном времени)
                    if (interval.trackSteps) {
                        val currentSteps = if (isActive) stateSteps else 0
                        Text(
                            text = stringResource(R.string.workout_interval_card_steps_label, currentSteps),
                            style = AppTypography.bodySmall,
                            color = if (showAsActive) AppSecondary else AppTextTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3. Время интервала
                Text(
                    text = formatDuration(interval.seconds),
                    style = AppTypography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (showAsActive) activeColor else AppTextPrimary
                )
            }
        }
    }
}
