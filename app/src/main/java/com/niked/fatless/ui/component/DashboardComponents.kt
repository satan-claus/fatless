package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.NutritionUiState

@Composable
fun DailySummaryCard(
    nutrition: NutritionUiState,
    steps: Int,
    stepGoal: Int,
    onClick: () -> Unit
) {
    val stepProgress = (steps.toFloat() / stepGoal).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NutritionalValueView(
                proteins = nutrition.totalProteins,
                fats = nutrition.totalFats,
                carbs = nutrition.totalCarbs,
                calories = nutrition.totalCalories,
                size = 100.dp
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Сегодня", style = AppTypography.titleMedium, color = AppTextPrimary)

                // Секция шагов
                Text(
                    text = "Шаги: $steps / $stepGoal", // Показываем цель
                    style = AppTypography.bodySmall,
                    color = AppSecondary,
                    fontWeight = FontWeight.Bold
                )

                // Индикатор прогресса
                LinearProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                        .height(6.dp),
                    color = AppSecondary,
                    trackColor = AppSecondary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )

                // Сводка БЖУ
                Text(
                    text = "Б:${nutrition.totalProteins.toInt()} Ж:${nutrition.totalFats.toInt()} У:${nutrition.totalCarbs.toInt()}",
                    style = AppTypography.bodySmall,
                    color = AppTextTertiary
                )
            }
        }
    }
}

@Composable
fun AddWorkoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("СОЗДАТЬ ТРЕНИРОВКУ", style = AppTypography.labelMedium)
    }
}
