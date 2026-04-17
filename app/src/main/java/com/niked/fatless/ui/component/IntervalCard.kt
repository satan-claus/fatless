package com.niked.fatless.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.niked.fatless.utils.formatDuration

@Composable
fun IntervalCard(
    interval: Interval,
    index: Int,
    isActive: Boolean,
    isCompleted: Boolean, // Интервал уже отработан
    state: WorkoutState,
    progress: Float
) {
    val isPaused = state is WorkoutState.PAUSED
    val isAllDone = state is WorkoutState.COMPLETED

    // Визуально активен (бордер + прогресс), если он текущий И мы еще не финишировали
    val showAsActive = isActive && !isAllDone

    // Определяем цвета (Оранж на паузе, Зеленый в работе)
    val activeColor = if (isPaused) AppOrange else AppPrimary
    val activeLightColor = if (isPaused) AppOrangeLight else AppPrimaryLight

    // Бледность для пройденных или если всё закончилось
    val containerAlpha = if (isCompleted || isAllDone) 0.55f else 1f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(containerAlpha),
        shape = RoundedCornerShape(12.dp),
        color = AppSurface,
        border = BorderStroke(1.5.dp, if (showAsActive) activeColor else Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Рисуем фон, если интервал активен
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
                // Кругляшок слева (Номер или Галка)
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = if (showAsActive) activeColor else AppBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // ГАЛКА: если этот шаг пройден ИЛИ вся тренировка завершена
                        if (isCompleted || isAllDone) {
                            val checkMarkColor = when {
                                isAllDone -> AppSecondary      // ФИНАЛ: Синий (Победный)
                                showAsActive -> Color.White    // На паузе: Белый
                                else -> AppTextTertiary        // В процессе (пройденные): Серый
                            }
                            Text(
                                text = "✓",
                                color = checkMarkColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } else {
                            // НОМЕР: только для текущего и будущих шагов
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

                Text(
                    text = interval.name,
                    style = AppTypography.labelLarge,
                    color = AppTextPrimary,
                    textDecoration = if (isCompleted || isAllDone) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )

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

