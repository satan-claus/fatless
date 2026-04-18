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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niked.fatless.ui.theme.*

@Composable
fun TimeStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    step: Int,
    minValue: Int = 0,
    maxValue: Int = 999
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Кнопка МИНУС
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = value >= minValue + step) { onValueChange(value - step) },
            shape = RoundedCornerShape(8.dp),
            color = if (value >= minValue + step) AppSurface else AppBackground,
            border = BorderStroke(1.dp, AppBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("-", color = if (value >= minValue + step) AppTextPrimary else AppTextTertiary, fontWeight = FontWeight.Bold)
            }
        }

        // ЗНАЧЕНИЕ
        Text(
            text = "$value $label",
            modifier = Modifier.padding(horizontal = 12.dp),
            style = AppTypography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppPrimary
        )

        // Кнопка ПЛЮС
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = value <= maxValue - step) { onValueChange(value + step) },
            shape = RoundedCornerShape(8.dp),
            color = if (value <= maxValue - step) AppSurface else AppBackground,
            border = BorderStroke(1.dp, AppBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("+", color = if (value <= maxValue - step) AppTextPrimary else AppTextTertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
