package com.niked.fatless.ui.component.fatlesshistory

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
    val goal = model.goal
    val totalFillRatio = (model.value / maxForScale).coerceIn(0f, 1f)
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
            if (model.value > goal) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(totalFillRatio)
                        .background(Color(0xFFFF00FF))
                )
            }

            // 2. БАЗОВЫЙ СЛОЙ (Зеленый/Оранжевый)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(totalFillRatio.coerceAtMost(goalRatio))
                    .background(model.barColor)
            )

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
    val totalFillRatio = if (maxCaloriesInWeek > 0) {
        totalCalories.toFloat() / maxCaloriesInWeek.coerceAtMost(1)
    } else 0f
    val sumNutrients = (proteins + fats + carbs).coerceAtLeast(1f)

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
            Column(
                modifier = Modifier
                    .fillMaxHeight(totalFillRatio.coerceIn(0.05f, 1f))
                    .fillMaxWidth()
            ) {
                Box(Modifier.fillMaxWidth().weight(carbs / sumNutrients + 0.01f).background(ColorCarbohydrates))
                Box(Modifier.fillMaxWidth().weight(fats / sumNutrients + 0.01f).background(ColorFats))
                Box(Modifier.fillMaxWidth().weight(proteins / sumNutrients + 0.01f).background(ColorProteins))
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
