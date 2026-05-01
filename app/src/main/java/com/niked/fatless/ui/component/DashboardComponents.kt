package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.ui.screen.animateFloatNumberAsState
import com.niked.fatless.ui.screen.animateNumberAsState
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppRed
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorSteps
import com.niked.fatless.ui.viewmodel.NutritionUiState
import kotlinx.coroutines.delay

@Composable
fun DailySummaryCard(
    nutrition: NutritionUiState,
    steps: Int,
    distance: Float,
    burnedCalories: Float,
    stepGoal: Int,
    onClick: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(steps, burnedCalories) {
        startAnim = false
        delay(50)
        startAnim = true
    }

    // 1. Анимируем шаги
    val animatedSteps by animateNumberAsState(
        targetValue = if (startAnim) steps else 0
    )

    // 2. Анимируем расход (сначала во Float, потом округлим)
    val animatedBurned by animateFloatNumberAsState(
        targetValue = if (startAnim) burnedCalories else 0f
    )

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
            // ЛЕВАЯ ЧАСТЬ: круг калорий/БЖУ
            NutritionalValueView(
                proteins = nutrition.totalProteins,
                fats = nutrition.totalFats,
                carbs = nutrition.totalCarbs,
                calories = nutrition.totalCalories.toInt(),
                size = 100.dp
            )

            Spacer(modifier = Modifier.width(20.dp))

            // ПРАВАЯ ЧАСТЬ: Шаги и Километры
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Сегодня",
                    style = AppTypography.titleMedium,
                    color = AppTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ШАГИ
                Text(
                    text = "$animatedSteps / $stepGoal шагов",
                    style = AppTypography.labelMedium,
                    color = ColorSteps
                )

                // ДИСТАНЦИЯ (Километры)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location_on_24),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.2f км", distance),
                        style = AppTypography.bodySmall,
                        color = AppTextSecondary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // 2. СОЖЖЕННЫЕ КАЛОРИИ (ОГОНЬ)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fire_24),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${animatedBurned.toInt()} ккал",
                        style = AppTypography.bodySmall,
                        color = AppTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LinearProgressIndicator для шагов
                OverstepLinearProgress(
                    steps = steps,
                    stepGoal = stepGoal
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
