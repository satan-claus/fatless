package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.ui.component.ActivityChart
import com.niked.fatless.ui.component.CalendarGrid
import com.niked.fatless.ui.component.DayHistoryDetails
import com.niked.fatless.ui.component.DaysOfWeekHeader
import com.niked.fatless.ui.component.MonthSummaryCard
import com.niked.fatless.ui.component.WeightChart
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.viewmodel.HistoryViewModel
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val month by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isSelectedToday = selectedDate == java.time.LocalDate.now()
    val monthData by viewModel.monthData.collectAsState()
    val selectedActivity by viewModel.selectedDayActivity.collectAsState()
    val weightData by viewModel.weightData.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()

    // Состояние скролла для всего экрана
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Шапка зафиксирована сверху
        WorkoutTopBar(
            title = stringResource(R.string.history_title),
            subTitle = "${month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()).replaceFirstChar { it.uppercase() }} ${month.year}",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { viewModel.prevMonth() }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = AppTextPrimary)
                }
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = AppTextPrimary)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            DaysOfWeekHeader()

            // РИСУЕМ КАЛЕНДАРЬ БЕЗ Lazy (через обычные Rows)
            CalendarGrid(
                month = month,
                selectedDate = selectedDate,
                monthData = monthData,
                onDateClick = { viewModel.selectDate(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MonthSummaryCard(summary = summary)

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Детали калорий и БЖУ
            DayHistoryDetails(activity = selectedActivity)

            // 2. График активности (столбики)
            if (selectedActivity != null) {
                ActivityChart(
                    hourlySteps = selectedActivity!!.hourlySteps,
                    isToday = isSelectedToday,
                    stepGoal = viewModel.stepGoal
                )
            }

            // 3. Динамика веса (линия)
            if (weightData.isNotEmpty()) {
                WeightChart(data = weightData)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
