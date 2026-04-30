package com.niked.fatless.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.niked.fatless.ui.theme.AppDisabledBg
import com.niked.fatless.ui.theme.ColorOverSteps
import com.niked.fatless.ui.theme.ColorStepsToday

@Composable
fun OverstepLinearProgress(
    steps: Int,
    stepGoal: Int,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    baseColor: Color = ColorStepsToday,
    overColor: Color = ColorOverSteps,
    trackColor: Color = AppDisabledBg.copy(alpha = 0.3f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val strokeWidth = size.height
        val gap = 4.dp.toPx() // Тот самый прозрачный промежуток

        // 1. ТРЕК (Серый фон)
        drawLine(
            color = trackColor,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            cap = StrokeCap.Round,
            strokeWidth = strokeWidth
        )

        // 2. БАЗОВЫЙ ПРОГРЕСС (Оранжевый)
        val progressRatio = (steps.toFloat() / stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
        if (progressRatio > 0.02f) {
            val progressEnd = size.width * progressRatio
            // Если прогресс не полный, вычитаем зазор для эффекта "воздуха"
            val finalEnd = if (progressRatio < 1f) (progressEnd - gap).coerceAtLeast(0f) else progressEnd

            drawLine(
                color = baseColor,
                start = Offset(0f, size.height / 2),
                end = Offset(finalEnd, size.height / 2),
                cap = StrokeCap.Round,
                strokeWidth = strokeWidth
            )
        }

        // 3. ОВЕРСТЕП (Фиолетовый поверх)
        if (steps > stepGoal) {
            val overRatio = ((steps - stepGoal).toFloat() / stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
            val overEnd = size.width * overRatio
            val finalOverEnd = if (overRatio < 1f) (overEnd - gap).coerceAtLeast(0f) else overEnd

            drawLine(
                color = overColor,
                start = Offset(0f, size.height / 2),
                end = Offset(finalOverEnd, size.height / 2),
                cap = StrokeCap.Round,
                strokeWidth = strokeWidth
            )
        }
    }
}
