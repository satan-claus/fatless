package com.niked.fatless.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // 1. Стейт для старта
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    // 2. Анимируем общий прогресс
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnim) (steps.toFloat() / stepGoal.coerceAtLeast(1)) else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "steps_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val strokeWidth = size.height
        val gap = 4.dp.toPx() // Тот самый прозрачный промежуток

        // 1. ТРЕК (Серый) - всегда на месте
        drawLine(
            color = trackColor,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            cap = StrokeCap.Round,
            strokeWidth = strokeWidth
        )

        // 2. БАЗОВЫЙ ПРОГРЕСС (Оранжевый)
        val baseRatio = animatedProgress.coerceIn(0f, 1f)
        if (baseRatio > 0.01f) {
            val endX = size.width * baseRatio
            drawLine(
                color = baseColor,
                start = Offset(0f, size.height / 2),
                end = Offset(if (baseRatio < 1f) (endX - gap).coerceAtLeast(0f) else endX, size.height / 2),
                cap = StrokeCap.Round,
                strokeWidth = strokeWidth
            )
        }

        // 3. ОВЕРСТЕП (Фиолетовый)
        if (animatedProgress > 1f) {
            val overRatio = (animatedProgress - 1f).coerceIn(0f, 1f)
            val overEndX = size.width * overRatio
            if (overRatio > 0.01f) {
                drawLine(
                    color = overColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(if (overRatio < 1f) (overEndX - gap).coerceAtLeast(0f) else overEndX, size.height / 2),
                    cap = StrokeCap.Round,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}
