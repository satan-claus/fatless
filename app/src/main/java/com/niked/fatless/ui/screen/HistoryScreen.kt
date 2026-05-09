package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.viewmodel.HistoryViewModel
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val month by viewModel.currentMonth.collectAsState()
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value // 1 (Пн) - 7 (Вс)

    // Сдвиг для сетки (если 1-е число — Четверг (4), то надо 3 пустых поля)
    val offset = firstDayOfWeek - 1

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        WorkoutTopBar(
            title = "История",
            subTitle = "${month.month.getDisplayName(TextStyle.FULL, Locale("ru"))} ${month.year}",
            onBackClick = onBackClick
        )

        // Сетка дней недели (Пн, Вт...)
//        DaysOfWeekHeader()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Пустые ячейки до начала месяца
            items(offset) { Spacer(modifier = Modifier.fillMaxWidth()) }

            // Числа месяца
            items(daysInMonth) { day ->
//                DayItem(day = day + 1)
            }
        }
    }
}
