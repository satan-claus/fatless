package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.ui.theme.AppDisabledBg
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorCarbohydrates
import com.niked.fatless.ui.theme.ColorFats
import com.niked.fatless.ui.theme.ColorProteins

@Composable
fun FatLessHistoryBar(
    model: HistoryBarModel,
    maxForScale: Float
) {
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnim = true
    }

    val targetTotalHeight = if (maxForScale > 0) model.value / maxForScale else 0f

    // АНИМАЦИЯ: срабатывает при каждой смене model.value
    val animatedHeight by animateFloatAsState(
        targetValue = if (startAnim) targetTotalHeight else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "step_growth"
    )

    val goal = model.goal
    val goalRatio = (goal / maxForScale).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Текст над столбиком
        Text(
            text = if (model.value > 0) "${(model.value / 1000).toInt()}k" else "",
            style = AppTypography.bodySmall,
            color = AppTextTertiary,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .width(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppDisabledBg.copy(alpha = 0.2f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 1. СЛОЙ ПЕРЕВЫПОЛНЕНИЯ (Розовый)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight.coerceIn(0f, 1f))
            ) {
                // Если есть перевыполнение, делим высоту между розовым и основным
                if (model.value > model.goal) {
                    val overflowWeight = (model.value - model.goal) / model.value
                    val baseWeight = model.goal / model.value

                    // Розовый (сверх)
                    Box(Modifier.fillMaxWidth().weight(overflowWeight).background(Color(0xFFFF00FF)))
                    // Основной (до цели)
                    Box(Modifier.fillMaxWidth().weight(baseWeight).background(model.barColor))
                } else {
                    // Просто основной столбик (если цель не достигнута)
                    Box(Modifier.fillMaxSize().background(model.barColor))
                }
            }

            // 3. КРАСНАЯ ЛИНИЯ ЦЕЛИ (Пунктир внутри каждого бокса)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineY = size.height * (1f - goalRatio)
                drawLine(
                    color = Color.Red,
                    start = Offset(-10f, lineY), // Хвосты линии по бокам
                    end = Offset(size.width + 10f, lineY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Звезда
            if (model.showStar) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (model.isToday) "Сегодня" else model.label,
            style = AppTypography.labelSmall,
            color = if (model.isToday) model.barColor else AppTextSecondary,
            fontWeight = if (model.isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StepChartWithGoalLine(data: List<HistoryBarModel>) {
    if (data.isEmpty()) return

    // Ищем максимум для масштабирования всей недели
    val goal = data.first().goal
    val maxVal = maxOf(data.maxOfOrNull { it.value } ?: 0f, goal).coerceAtLeast(1f)

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { model ->
            FatLessHistoryBar(model = model, maxForScale = maxVal)
        }
    }
}

@Composable
fun NutritionStackedBar(
    dayLabel: String,
    proteins: Float,
    fats: Float,
    carbs: Float,
    totalCalories: Int,
    maxCaloriesInWeek: Int,
    isToday: Boolean
) {
    // Стейт для старта анимации
    var startAnim by remember { mutableStateOf(false) }

    // Запускаем рост сразу после появления
    LaunchedEffect(Unit) {
        startAnim = true
    }

    val totalFillRatio = if (maxCaloriesInWeek > 0) {
        totalCalories.toFloat() / maxCaloriesInWeek.coerceAtLeast(1)
    } else 0f

    val sumNutrients = (proteins + fats + carbs).coerceAtLeast(1f)

    val animatedHeight by animateFloatAsState(
        targetValue = if (startAnim) totalFillRatio else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "nutrition_growth"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = totalCalories.toString(),
            style = AppTypography.bodySmall,
            color = AppTextSecondary,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .width(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppDisabledBg.copy(alpha = 0.3f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (totalCalories > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(animatedHeight.coerceIn(0.01f, 1f))
                        .fillMaxWidth()
                ) {
                    // БЖУ СЛОИ
                    Box(Modifier.fillMaxWidth().weight((carbs / sumNutrients).coerceAtLeast(0.01f)).background(ColorCarbohydrates))
                    Box(Modifier.fillMaxWidth().weight((fats / sumNutrients).coerceAtLeast(0.01f)).background(ColorFats))
                    Box(Modifier.fillMaxWidth().weight((proteins / sumNutrients).coerceAtLeast(0.01f)).background(ColorProteins))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isToday) "Сегодня" else dayLabel,
            style = AppTypography.labelSmall,
            color = if (isToday) AppPrimary else AppTextSecondary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

