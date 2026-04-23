package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.ui.theme.*
import com.niked.fatless.core.utils.formatDuration

@Composable
fun WorkoutRow(
    workout: Workout,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = AppSurface,
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Название тренировки (Title Medium: Roboto 16 SemiBold)
                Text(
                    text = workout.title,
                    style = AppTypography.titleMedium,
                    color = AppTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Метаданные (Body Small: Roboto 13 Medium)
                Text(
                    text = "${workout.intervals.size} интервалов • ${formatDuration(workout.intervals.sumOf { it.seconds })}",
                    style = AppTypography.bodySmall,
                    color = AppTextTertiary
                )
            }
            // Стрелочка вправо
            Text(
                text = "›",
                fontSize = 24.sp,
                color = AppTextTertiary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
