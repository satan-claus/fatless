package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.domain.repository.IActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val activityRepository: IActivityRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    val monthData = _currentMonth.flatMapLatest { month ->
        activityRepository.getActivityForMonth(month.toString())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedDayActivity: StateFlow<DailyActivity?> = combine(
        monthData,
        _selectedDate
    ) { data, selected ->
        data.find { it.date == selected.toString() }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

//    val weightData: StateFlow<List<DailyActivity>> = monthData.map { list ->
//        list.filter { it.weight > 0 }.sortedBy { it.date }
//    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ВРЕМЕННО для теста (потом удалишь)
    val weightData: StateFlow<List<DailyActivity>> = MutableStateFlow(
        listOf(
            DailyActivity(date = "2024-05-01", steps = 0, consumedCalories = 0f, burnedCalories = 0f, proteins = 0f, fats = 0f, carbs = 0f, weight = 85.5f),
            DailyActivity(date = "2024-05-03", steps = 0, consumedCalories = 0f, burnedCalories = 0f, proteins = 0f, fats = 0f, carbs = 0f, weight = 84.8f),
            DailyActivity(date = "2024-05-05", steps = 0, consumedCalories = 0f, burnedCalories = 0f, proteins = 0f, fats = 0f, carbs = 0f, weight = 85.2f),
            DailyActivity(date = "2024-05-07", steps = 0, consumedCalories = 0f, burnedCalories = 0f, proteins = 0f, fats = 0f, carbs = 0f, weight = 83.9f)
        )
    ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun prevMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
}
