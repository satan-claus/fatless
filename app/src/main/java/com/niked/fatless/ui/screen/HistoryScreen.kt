package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.niked.fatless.ui.component.DayHistoryDetails
import com.niked.fatless.ui.component.DayItem
import com.niked.fatless.ui.component.DaysOfWeekHeader
import com.niked.fatless.ui.component.WeightChart
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val month by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val monthData by viewModel.monthData.collectAsState()
    val selectedActivity by viewModel.selectedDayActivity.collectAsState()

    // Настройка календаря
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value // 1 (Пн) - 7 (Вс)
    val offset = firstDayOfWeek - 1 // Сколько пустых ячеек в начале

    // Локализация названия месяца (Май 2024)
    val monthTitle = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        // --- ШАПКА С КНОПКАМИ ---
        WorkoutTopBar(
            title = stringResource(R.string.history_title),
            subTitle = "$monthTitle ${month.year}",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { viewModel.prevMonth() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.content_description_prev_month),
                        tint = AppTextPrimary
                    )
                }
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.content_description_next_month),
                        tint = AppTextPrimary
                    )
                }
            }
        )

        // --- ДНИ НЕДЕЛИ (Пн, Вт...) ---
        DaysOfWeekHeader()

        // --- СЕТКА КАЛЕНДАРЯ ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Пустые ячейки (сдвиг)
            items(offset) {
                Spacer(modifier = Modifier.aspectRatio(1f))
            }

            // 2. Числа месяца
            items(daysInMonth) { index ->
                val dayNumber = index + 1
                val date = month.atDay(dayNumber)

                // Проверяем активность в этот день
                val hasActivity = monthData.any { it.date == date.toString() }
                val isToday = date == LocalDate.now()

                DayItem(
                    day = dayNumber,
                    isSelected = selectedDate == date,
                    hasData = hasActivity,
                    isToday = isToday,
                    onClick = { viewModel.selectDate(date) }
                )
            }
        }

        DayHistoryDetails(activity = selectedActivity)

        val weightData by viewModel.weightData.collectAsState()

        if (weightData.isNotEmpty()) {
            WeightChart(data = weightData)
        }
    }
}
