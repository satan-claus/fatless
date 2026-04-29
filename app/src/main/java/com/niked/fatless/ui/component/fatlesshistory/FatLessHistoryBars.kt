package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.ui.theme.*

// 1. ТЕЛО ДЛЯ ШАГОВ (Одиночный столб + Звезда)
@Composable
fun FatLessHistoryBar(
    model: HistoryBarModel,
    maxInPeriod: Float,
    modifier: Modifier = Modifier
) {
    val fillRatio = if (maxInPeriod > 0) model.value / maxInPeriod else 0f

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxHeight()) {
        Text(
            text = if (model.value >= 1000) "${(model.value / 1000).toInt()}k" else model.value.toInt().toString(),
            style = AppTypography.bodySmall, color = AppTextTertiary, fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.weight(1f).width(28.dp).clip(RoundedCornerShape(8.dp)).background(AppDisabledBg.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxHeight(fillRatio.coerceIn(0.05f, 1f)).fillMaxWidth().background(model.barColor)) {
                if (model.showStar) {
                    Icon(
                        imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).size(14.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (model.isToday) "Сегодня" else model.label,
            style = AppTypography.labelSmall, color = if (model.isToday) AppTextPrimary else AppTextSecondary,
            fontWeight = if (model.isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// 2. ТЕЛО ДЛЯ ПИТАНИЯ (Слои БЖУ а-ля MIUI)
@Composable
fun NutritionStackedBar(
    dayLabel: String,
    proteins: Float,
    fats: Float,
    carbs: Float,
    totalCalories: Int,
    maxCaloriesInWeek: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val totalFillRatio = if (maxCaloriesInWeek > 0) totalCalories.toFloat() / maxCaloriesInWeek.coerceAtMost(1) else 0f
    val sumNutrients = (proteins + fats + carbs).coerceAtLeast(1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxHeight()) {
        Text(text = totalCalories.toString(), style = AppTypography.bodySmall, color = AppTextSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.weight(1f).width(30.dp).clip(RoundedCornerShape(8.dp)).background(AppDisabledBg.copy(alpha = 0.3f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(modifier = Modifier.fillMaxHeight(totalFillRatio.coerceIn(0.05f, 1f)).fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().weight(carbs / sumNutrients + 0.01f).background(ColorCarbohydrates))
                Box(Modifier.fillMaxWidth().weight(fats / sumNutrients + 0.01f).background(ColorFats))
                Box(Modifier.fillMaxWidth().weight(proteins / sumNutrients + 0.01f).background(ColorProteins))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isToday) "Сегодня" else dayLabel,
            style = AppTypography.labelSmall, color = if (isToday) AppPrimary else AppTextSecondary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
