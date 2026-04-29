package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val pageCount by viewModel.pageCount.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = (pageCount - 1).coerceAtLeast(0),
        pageCount = { pageCount }
    )

    // Синхронизация пейджера с вьюмоделью для подгрузки нужной недели
    LaunchedEffect(pagerState.currentPage, pageCount) {
        viewModel.updateOffsetFromPage(pagerState.currentPage, pageCount)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- ШАПКА ---
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

            // --- ГРАФИК (С Пейджером) ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Crossfade(targetState = historyType, label = "chart_anim") { type ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (type == FatLessHistoryType.STEPS) {
                            StepChartWithGoalLine(data = chartData.first)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
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
                }
            }

            // --- ПОДВАЛ ---
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
