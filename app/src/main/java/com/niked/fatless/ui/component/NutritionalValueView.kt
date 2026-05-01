package com.niked.fatless.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorCarbohydrates
import com.niked.fatless.ui.theme.ColorFats
import com.niked.fatless.ui.theme.ColorProteins
import kotlinx.coroutines.delay

@Composable
fun NutritionalValueView(
    modifier: Modifier = Modifier,
    proteins: Float,
    fats: Float,
    carbs: Float,
    calories: Int,
    size: Dp = 200.dp
) {
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(calories) {
        startAnim = false
        delay(50)
        startAnim = true
    }

    val globalProgress by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "global_growth"
    )

    val scaleFactor = size.value / 200f
    val fontSizeCalories = (28f * scaleFactor).coerceAtLeast(14f).sp
    val fontSizeLabel = (12f * scaleFactor).coerceAtLeast(9f).sp
    val strokeWidthPx = 45f * scaleFactor

    val total = (proteins + fats + carbs).coerceAtLeast(0.1f)
    val isEmpty = proteins == 0f && fats == 0f && carbs == 0f

    val pProp by animateFloatAsState(if (isEmpty) 0.33f else proteins / total, tween(1000), label = "p")
    val fProp by animateFloatAsState(if (isEmpty) 0.33f else fats / total, tween(1000), label = "f")
    val cProp by animateFloatAsState(if (isEmpty) 0.34f else carbs / total, tween(1000), label = "c")

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = size.toPx() - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            val drawingSize = Size(arcSize, arcSize)

            val pColor = if (isEmpty) AppBorder else ColorProteins
            val fColor = if (isEmpty) AppBorder else ColorFats
            val cColor = if (isEmpty) AppBorder else ColorCarbohydrates

            val gap = 3f
            val pSweepFinal = pProp * 360f
            val fSweepFinal = fProp * 360f
            val cSweepFinal = cProp * 360f

            // 1. БЕЛКИ
            drawArc(
                color = pColor,
                startAngle = -90f + (gap / 2f),
                sweepAngle = ((pSweepFinal - gap) * globalProgress).coerceAtLeast(0.1f),
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidthPx)
            )

            // 2. ЖИРЫ
            drawArc(
                color = fColor,
                startAngle = -90f + pSweepFinal + (gap / 2f),
                sweepAngle = ((fSweepFinal - gap) * globalProgress).coerceAtLeast(0.1f),
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidthPx)
            )

            // 3. УГЛЕВОДЫ
            drawArc(
                color = cColor,
                startAngle = -90f + pSweepFinal + fSweepFinal + (gap / 2f),
                sweepAngle = ((cSweepFinal - gap) * globalProgress).coerceAtLeast(0.1f),
                useCenter = false,
                topLeft = topLeft,
                size = drawingSize,
                style = Stroke(width = strokeWidthPx)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val animatedCalories = (calories * globalProgress).toInt()
            Text(
                text = if (isEmpty) "0" else animatedCalories.toString(),
                style = AppTypography.titleLarge.copy(fontSize = fontSizeCalories),
                color = if (isEmpty) AppTextTertiary else AppTextPrimary
            )
            Text(
                text = "кКал",
                style = AppTypography.bodySmall.copy(fontSize = fontSizeLabel),
                color = AppTextTertiary
            )
        }
    }
}
