package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import kotlinx.coroutines.launch

@Composable
fun FatLessHistoryComponent(
    viewModel: FatLessHistoryViewModel = hiltViewModel()
) {
    val historyType by viewModel.historyType.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val weekRange by viewModel.weekRange.collectAsState()
    val scope = rememberCoroutineScope()

    // 1. Инициализируем стейт на последней странице
    val pagerState = rememberPagerState(
        initialPage = (pageCount - 1).coerceAtLeast(0),
        pageCount = { pageCount }
    )

    // Флаг, чтобы автоскролл сработал строго ОДИН РАЗ при запуске
    var hasInitialScrollDone by remember { mutableStateOf(false) }

    // 2. Автоскролл к последней странице при первой загрузке
    LaunchedEffect(pageCount) {
        if (pageCount > 1 && !hasInitialScrollDone) {
            pagerState.scrollToPage(pageCount - 1)
            hasInitialScrollDone = true
        }
    }

    // 3. Синхронизируем офсет для заголовка дат при свайпах
    LaunchedEffect(pagerState.currentPage, pageCount) {
        viewModel.updateOffsetFromPage(pagerState.currentPage, pageCount)
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp),
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
                // Группируем текст слева
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (historyType == FatLessHistoryType.STEPS)
                            stringResource(R.string.history_title_steps)
                        else
                            stringResource(R.string.history_title_nutrition),
                        style = AppTypography.titleSmall,
                        color = AppTextPrimary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = weekRange,
                        style = AppTypography.bodySmall,
                        color = AppTextTertiary,
                        maxLines = 1
                    )
                }

                // Иконки переключения (всегда справа)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.setHistoryType(FatLessHistoryType.STEPS) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (historyType == FatLessHistoryType.STEPS)
                                    AppPrimary.copy(alpha = 0.12f)
                                else
                                    Color.Transparent
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_directions_walk_24_green),
                            contentDescription = stringResource(R.string.content_description_switch_to_steps),
                            tint = if (historyType == FatLessHistoryType.STEPS) AppPrimary else AppTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { viewModel.setHistoryType(FatLessHistoryType.NUTRITION) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (historyType == FatLessHistoryType.NUTRITION)
                                    AppPrimary.copy(alpha = 0.12f)
                                else
                                    Color.Transparent
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_restaurant_24),
                            contentDescription = stringResource(R.string.content_description_switch_to_nutrition),
                            tint = if (historyType == FatLessHistoryType.NUTRITION) AppPrimary else AppTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ГРАФИК (С Пейджером) ---
            // Обертка для стрелок поверх пейджера
            Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp)) {

                // --- 1. ЛЕВАЯ СТРЕЛКА (Листать назад) ---
                // Показываем только если текущая страница БОЛЬШЕ нуля
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            // Выносим поверх графика, чтобы клик не перехватывался
                            .zIndex(1f)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.content_description_prev_week),
                            tint = AppTextTertiary
                        )
                    }
                }

                // --- 2. САМ ТВОЙ ПЕЙДЖЕР (С небольшими падингами по бокам под стрелки) ---
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    key = { page -> page }
                ) { page ->
                    val weekData = viewModel.getWeekData(page, pageCount, allHistory)
                    val stepData = weekData.first
                    val nutritionData = weekData.second

                    Crossfade(targetState = historyType, label = "chart_anim") { type ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (type == FatLessHistoryType.STEPS) {
                                StepChartWithGoalLine(data = stepData)
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val maxCal = nutritionData.maxOfOrNull { it.totalCalories } ?: 1f
                                    nutritionData.forEach { model ->
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

                // --- 3. ПРАВАЯ СТРЕЛКА (Листать вперед) ---
                // Показываем только если текущая страница МЕНЬШЕ, чем максимальный индекс (pageCount - 1)
                if (pagerState.currentPage < pageCount - 1) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .zIndex(1f) // Выносим поверх графика
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.content_description_next_week),
                            tint = AppTextTertiary
                        )
                    }
                }
            }

            // --- ПОДВАЛ ---
            Spacer(modifier = Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val currentData = viewModel.getWeekData(pagerState.currentPage, pageCount, allHistory)
                val avg = if (historyType == FatLessHistoryType.STEPS) {
                    val steps = currentData.first.map { it.value }.filter { it > 0 }
                    if (steps.isNotEmpty()) steps.average().toInt() else 0
                } else {
                    val cals = currentData.second.map { it.totalCalories }.filter { it > 0 }
                    if (cals.isNotEmpty()) cals.average().toInt() else 0
                }

                Text(
                    text = stringResource(R.string.history_average_label, avg),
                    style = AppTypography.bodySmall,
                    color = AppTextTertiary
                )
            }
        }
    }
}
