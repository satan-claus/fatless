package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val activityRepository: IActivityRepository,
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthData: StateFlow<List<DailyActivity>> = _currentMonth.flatMapLatest { month ->
        activityRepository.getActivityForMonth(month.toString())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val stepGoal = settingsRepository.stepGoal

    // 🎯 ИТОГИ МЕСЯЦА
    val monthSummary: StateFlow<MonthSummary?> = monthData.map { data ->
        if (data.isEmpty()) return@map null

        val totalSteps = data.sumOf { it.steps }
        val stride = settingsRepository.userHeight * 0.415f
        val totalKm = (totalSteps * stride) / 100_000f

        val weights = data.filter { it.weight > 0 }.map { it.weight }
        val avgWeight = if (weights.isNotEmpty()) weights.average().toFloat() else settingsRepository.userWeight

        val goalReachedDays = data.count { it.steps >= settingsRepository.stepGoal }

        MonthSummary(
            totalKm = totalKm,
            avgWeight = avgWeight,
            goalReachedDays = goalReachedDays
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDayActivity: StateFlow<DailyActivity?> = combine(
        monthData,
        _selectedDate
    ) { data, selected ->
        val dbActivity = data.find { it.date == selected.toString() }
        val today = LocalDate.now()

        if (selected == today) {
            DailyActivity(
                date = selected.toString(),
                steps = settingsRepository.todaySteps,
                consumedCalories = dbActivity?.consumedCalories ?: 0f,
                burnedCalories = settingsRepository.todayBurnedCalories,
                proteins = dbActivity?.proteins ?: 0f,
                fats = dbActivity?.fats ?: 0f,
                carbs = dbActivity?.carbs ?: 0f,
                weight = settingsRepository.userWeight,
                hourlySteps = settingsRepository.todayHourlySteps
            )
        } else {
            dbActivity
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val weightData: StateFlow<List<DailyActivity>> = monthData.map { list ->
        list.filter { it.weight > 0 }.sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // 🎯 СИНХРОНИЗАЦИЯ: при создании вьюмодели обновляем запись за сегодня в БД
        syncCurrentDayToDatabase()
    }

    private fun syncCurrentDayToDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            activityRepository.saveSteps(
                date = LocalDate.now().toString(),
                steps = settingsRepository.todaySteps,
                burnedCalories = settingsRepository.todayBurnedCalories,
                currentWeight = settingsRepository.userWeight,
                hourlySteps = settingsRepository.todayHourlySteps
            )
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun prevMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
}

data class MonthSummary(
    val totalKm: Float,
    val avgWeight: Float,
    val goalReachedDays: Int
)
