package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.domain.model.ExerciseType
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppError
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun EditableIntervalItem(
    index: Int,
    interval: Interval,
    allExerciseTypes: List<ExerciseType>,
    onNameChange: (String) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onTrackStepsChange: (Boolean) -> Unit,
    onExerciseTypeChange: (ExerciseType) -> Unit,
    onRemove: () -> Unit
) {
    // Вычисляем заголовок для чекбокса
    val stepsLabel = if (interval.trackSteps) {
        stringResource(R.string.workout_interval_status_steps_active)
    } else {
        stringResource(R.string.workout_interval_status_steps_off)
    }

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
                    placeholder = { Text(stringResource(R.string.workout_interval_name_placeholder), color = AppTextTertiary) },
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
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.content_description_remove_interval),
                        tint = AppError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppBorder.copy(alpha = 0.5f))

            // НИЖНИЙ РЯД: Настройки времени и повторов
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. ВРЕМЯ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.workout_interval_label_time),
                        style = AppTypography.titleSmall,
                        color = AppTextSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        TimeStepper(
                            label = stringResource(R.string.workout_interval_unit_minutes),
                            value = interval.seconds / 60,
                            onValueChange = { onSecondsChange(it * 60 + (interval.seconds % 60)) },
                            step = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        TimeStepper(
                            label = stringResource(R.string.workout_interval_unit_seconds),
                            value = interval.seconds % 60,
                            onValueChange = { onSecondsChange((interval.seconds / 60) * 60 + it) },
                            step = 5,
                            maxValue = 55
                        )
                    }
                }

                // 2. ПОВТОРЫ (СИЛОВАЯ ЦЕЛЬ)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = interval.reps != null,
                        onCheckedChange = { onRepsChange(if (it) 10 else null) },
                        colors = CheckboxDefaults.colors(checkedColor = AppOrange)
                    )
                    Text(stringResource(R.string.workout_interval_label_goal), style = AppTypography.titleSmall, color = AppTextSecondary, modifier = Modifier.width(44.dp))

                    if (interval.reps != null) {
                        TimeStepper(
                            label = stringResource(R.string.workout_interval_unit_reps),
                            value = interval.reps ?: 0,
                            onValueChange = { onRepsChange(it) },
                            step = 5,
                            minValue = 1
                        )
                    } else {
                        Text(stringResource(R.string.workout_interval_status_time_only), style = AppTypography.bodySmall, color = AppTextTertiary)
                    }
                }

                // 3. ШАГОМЕР (КАРДИО ЦЕЛЬ)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = interval.trackSteps,
                        onCheckedChange = { onTrackStepsChange(it) },
                        colors = CheckboxDefaults.colors(checkedColor = AppSecondary)
                    )
                    Text(stringResource(R.string.workout_interval_label_steps), style = AppTypography.titleSmall, color = AppTextSecondary, modifier = Modifier.width(44.dp))

                    Text(
                        text = stepsLabel,
                        style = AppTypography.bodySmall,
                        color = if (interval.trackSteps) AppSecondary else AppTextTertiary
                    )
                }

                if (interval.trackSteps && allExerciseTypes.isNotEmpty()) {
                    ExerciseModeSelector(
                        modifier = Modifier.padding(start = 8.dp),
                        types = allExerciseTypes,
                        selectedId = interval.exerciseType?.id,
                        onSelect = { id ->
                            val selectedType = allExerciseTypes.find { it.id == id }
                            if (selectedType != null) {
                                onExerciseTypeChange(selectedType)
                            }
                        }
                    )
                }
            }
        }
    }
}
