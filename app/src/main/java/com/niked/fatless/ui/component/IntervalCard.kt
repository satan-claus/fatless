package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.theme.*
import com.niked.fatless.core.utils.formatDuration

@Composable
fun IntervalCard(
    interval: Interval,
    index: Int,
    isActive: Boolean,
    isCompleted: Boolean,
    state: WorkoutState,
    progress: Float,
    onClick: () -> Unit = {}
) {
    val isPaused = state is WorkoutState.PAUSED
    val isAllDone = state is WorkoutState.COMPLETED

    // Активен ли визуально этот интервал (с бордером и закраской)
    val showAsActive = isActive && !isAllDone

    // Цвета (Оранж на паузе, Зеленый в работе)
    val activeColor = if (isPaused) AppOrange else AppPrimary
    val activeLightColor = if (isPaused) AppOrangeLight else AppPrimaryLight

    // Бледность для пройденных или если всё закончилось
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
                    // Рисуем заполнение фона прогрессом
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

                // 2. Название + Цель (Повторы)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = interval.name,
                        style = AppTypography.labelLarge,
                        color = AppTextPrimary,
                        textDecoration = if (isCompleted || isAllDone) TextDecoration.LineThrough else null
                    )

                    // Если заданы повторы — показываем их второй строчкой
                    if (interval.reps != null && interval.reps > 0) {
                        Text(
                            text = "Цель: ${interval.reps} повт.",
                            style = AppTypography.bodySmall,
                            color = if (showAsActive) activeColor else AppOrange,
                            fontWeight = FontWeight.Medium
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
