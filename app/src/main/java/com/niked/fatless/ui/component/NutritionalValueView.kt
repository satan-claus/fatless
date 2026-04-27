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

    // Анимируем доли (от 0 до 1)
    val pProp by animateFloatAsState(if (isEmpty) 0.33f else proteins / total, tween(1000))
    val fProp by animateFloatAsState(if (isEmpty) 0.33f else fats / total, tween(1000))
    val cProp by animateFloatAsState(if (isEmpty) 0.34f else carbs / total, tween(1000))

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 45f
            val arcSize = size.toPx() - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val drawingSize = Size(arcSize, arcSize)

            val pColor = if (isEmpty) AppBorder else ColorProteins
            val fColor = if (isEmpty) AppBorder else ColorFats
            val cColor = if (isEmpty) AppBorder else ColorCarbohydrates

            // Зазор между секторами в градусах
            val gap = 3f

            // 1. БЕЛКИ (Зеленый)
            val pSweep = (pProp * 360f)
            drawArc(
                color = pColor,
                startAngle = -90f + (gap / 2f),
                sweepAngle = (pSweep - gap).coerceAtLeast(0.1f),
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidth) // Убрали Round, теперь стыки честные
            )

            // 2. ЖИРЫ (Желтый)
            val fSweep = (fProp * 360f)
            drawArc(
                color = fColor,
                startAngle = -90f + pSweep + (gap / 2f),
                sweepAngle = (fSweep - gap).coerceAtLeast(0.1f),
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidth)
            )

            // 3. УГЛЕВОДЫ (Красный)
            val cSweep = (cProp * 360f)
            drawArc(
                color = cColor,
                startAngle = -90f + pSweep + fSweep + (gap / 2f),
                sweepAngle = (cSweep - gap).coerceAtLeast(0.1f),
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidth)
            )
        }

        // Калории в центре
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isEmpty) "0" else calories.toString(),
                style = AppTypography.titleLarge.copy(fontSize = 32.sp),
                color = if (isEmpty) AppTextTertiary else AppTextPrimary
            )
            Text(text = "кКал", style = AppTypography.bodySmall, color = AppTextTertiary)
        }
    }
}

