package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun FatLessHistoryComponent(
    viewModel: FatLessHistoryViewModel = hiltViewModel()
) {
    val historyType by viewModel.historyType.collectAsState()
    val chartData by viewModel.chartData.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ШАПКА
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (historyType == FatLessHistoryType.STEPS) "Активность" else "Питание",
                    style = AppTypography.titleSmall,
                    color = AppTextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.setHistoryType(FatLessHistoryType.STEPS) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_directions_walk_24),
                            contentDescription = null,
                            tint = if (historyType == FatLessHistoryType.STEPS) AppPrimary else AppTextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.setHistoryType(FatLessHistoryType.NUTRITION) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_restaurant_24),
                            contentDescription = null,
                            tint = if (historyType == FatLessHistoryType.NUTRITION) AppPrimary else AppTextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ГРАФИК
            Crossfade(targetState = historyType, label = "chart_anim") { type ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (type == FatLessHistoryType.STEPS) {
                        val maxSteps = chartData.first.maxOfOrNull { it.value } ?: 1f
                        chartData.first.forEach { model ->
                            FatLessHistoryBar(model = model, maxInPeriod = maxSteps)
                        }
                    } else {
                        val maxCal = chartData.second.maxOfOrNull { it.totalCalories } ?: 1
                        chartData.second.forEach { model ->
                            NutritionStackedBar(
                                dayLabel = model.dayLabel,
                                proteins = model.proteins,
                                fats = model.fats,
                                carbs = model.carbs,
                                totalCalories = model.totalCalories,
                                maxCaloriesInWeek = maxCal,
                                isToday = model.isToday
                            )
                        }
                    }
                }
            }

            // ПОДВАЛ
            Spacer(modifier = Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val avg = if (historyType == FatLessHistoryType.STEPS) {
                    if (chartData.first.isNotEmpty()) chartData.first.map { it.value }.average().toInt() else 0
                } else {
                    if (chartData.second.isNotEmpty()) chartData.second.map { it.totalCalories }.average().toInt() else 0
                }
                Text(
                    text = "В среднем: $avg",
                    style = AppTypography.bodySmall,
                    color = AppTextTertiary
                )
            }
        }
    }
}
