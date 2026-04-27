package com.niked.fatless.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.ui.theme.*

@Composable
fun NutritionalValueView(
    modifier: Modifier = Modifier,
    proteins: Float,
    fats: Float,
    carbs: Float,
    calories: Int,
    size: Dp = 200.dp
) {
    val total = proteins + fats + carbs
    val isEmpty = total == 0f

    // Анимируем углы. Если пусто — фиксируем по 120 (с учетом зазоров)
    val animatedProteins by animateFloatAsState(
        targetValue = if (isEmpty) 118f else (proteins / total) * 360f,
        animationSpec = tween(1000)
    )
    val animatedFats by animateFloatAsState(
        targetValue = if (isEmpty) 118f else (fats / total) * 360f,
        animationSpec = tween(1000)
    )
    val animatedCarbs by animateFloatAsState(
        targetValue = if (isEmpty) 118f else (carbs / total) * 360f,
        animationSpec = tween(1000)
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 40f // Сделал чуть тоньше для изящности
            val arcSize = size.toPx() - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val drawingSize = Size(arcSize, arcSize)

            // Если пусто — используем серый, если нет — наши сочные цвета
            val pColor = if (isEmpty) AppBorder else ColorProteins
            val fColor = if (isEmpty) AppBorder else ColorFats
            val cColor = if (isEmpty) AppBorder else ColorCarbohydrates

            // Зазор между секторами (в градусах)
            val gap = 2f

            // Белки
            drawArc(
                color = pColor,
                startAngle = -90f + (gap / 2),
                sweepAngle = animatedProteins - gap,
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round) // Скругленные края добавят стиля
            )

            // Жиры
            drawArc(
                color = fColor,
                startAngle = -90f + animatedProteins + (gap / 2),
                sweepAngle = animatedFats - gap,
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Углеводы
            drawArc(
                color = cColor,
                startAngle = -90f + animatedProteins + animatedFats + (gap / 2),
                sweepAngle = animatedCarbs - gap,
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Центр (Калории)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isEmpty) "0" else calories.toString(),
                style = AppTypography.titleLarge.copy(fontSize = 32.sp),
                color = if (isEmpty) AppTextTertiary else AppTextPrimary
            )
            Text(
                text = "кКал",
                style = AppTypography.bodySmall,
                color = AppTextTertiary
            )
        }
    }
}
