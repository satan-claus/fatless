package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.ui.theme.*

@Composable
fun EditableIntervalItem(
    index: Int,
    interval: Interval,
    onNameChange: (String) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onTrackStepsChange: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ВЕРХНИЙ РЯД: Имя и Удаление
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${index + 1}", style = AppTypography.bodySmall, modifier = Modifier.width(24.dp))

                TextField(
                    value = interval.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Назови эту пытку...", color = AppTextTertiary) },
                    textStyle = AppTypography.bodyLarge.copy(color = AppTextPrimary),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = AppError, modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppBorder.copy(alpha = 0.5f))

            // НИЖНИЙ РЯД: Настройки времени и повторов
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. ВРЕМЯ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ВРЕМЯ:", style = AppTypography.titleSmall, color = AppTextSecondary, modifier = Modifier.width(60.dp))

                    TimeStepper(
                        label = "мин",
                        value = interval.seconds / 60,
                        onValueChange = { onSecondsChange(it * 60 + (interval.seconds % 60)) },
                        step = 1
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TimeStepper(
                        label = "сек",
                        value = interval.seconds % 60,
                        onValueChange = { onSecondsChange((interval.seconds / 60) * 60 + it) },
                        step = 5,
                        maxValue = 55
                    )
                }

                // 2. ПОВТОРЫ (СИЛОВАЯ ЦЕЛЬ)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = interval.reps != null,
                        onCheckedChange = { onRepsChange(if (it) 10 else null) },
                        colors = CheckboxDefaults.colors(checkedColor = AppOrange)
                    )
                    Text("ЦЕЛЬ:", style = AppTypography.titleSmall, color = AppTextSecondary, modifier = Modifier.width(44.dp))

                    if (interval.reps != null) {
                        TimeStepper(
                            label = "повт",
                            value = interval.reps ?: 0,
                            onValueChange = { onRepsChange(it) },
                            step = 5,
                            minValue = 1
                        )
                    } else {
                        Text("только время", style = AppTypography.bodySmall, color = AppTextTertiary)
                    }
                }

                // 3. ШАГОМЕР (КАРДИО ЦЕЛЬ)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = interval.trackSteps,
                        onCheckedChange = { onTrackStepsChange(it) },
                        colors = CheckboxDefaults.colors(checkedColor = AppSecondary)
                    )
                    Text("ШАГИ:", style = AppTypography.titleSmall, color = AppTextSecondary, modifier = Modifier.width(44.dp))

                    Text(
                        text = if (interval.trackSteps) "активно" else "выключено",
                        style = AppTypography.bodySmall,
                        color = if (interval.trackSteps) AppSecondary else AppTextTertiary
                    )
                }
            }
        }
    }
}
