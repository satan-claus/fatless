package com.niked.fatless.ui.component.fatlesshistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.data.local.entities.DailyActivityEntity
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FatLessHistoryViewModel @Inject constructor(
    private val repository: IActivityRepository,
    val settings: AppSettings
) : ViewModel() {

    private val _historyType = MutableStateFlow(FatLessHistoryType.STEPS)
    val historyType: StateFlow<FatLessHistoryType> = _historyType.asStateFlow()

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    // ДИАПАЗОН ДАТ: "27 апр — 03 мая"
    val weekRange: StateFlow<String> = _weekOffset.map { offset ->
        val today = LocalDate.now()
        val monday = today
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(offset.toLong())
        val sunday = monday.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))
        "${monday.format(formatter)} — ${sunday.format(formatter)}"
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    // 🎯 КОЛИЧЕСТВО СТРАНИЦ
    val pageCount = repository.getActivityHistory().map { history ->
        if (history.isEmpty()) return@map 3
        val firstDate = LocalDate.parse(history.minOf { it.date })
        val today = LocalDate.now()
        val mondayOfFirstWeek = firstDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeksBetween = ChronoUnit.WEEKS.between(mondayOfFirstWeek, today).toInt()
        (weeksBetween + 1 + 2).coerceAtLeast(3)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 3)

    // 🎯 ДАННЫЕ ГРАФИКА
    val chartData = combine(
        repository.getActivityHistory(),
        _historyType,
        _weekOffset
    ) { history, type, offset ->
        prepareData(history, type, offset)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Pair(emptyList<HistoryBarModel>(), emptyList<NutritionBarModel>())
    )

    fun setHistoryType(type: FatLessHistoryType) {
        _historyType.value = type
    }

    fun updateOffsetFromPage(page: Int, totalPages: Int) {
        _weekOffset.value = page - (totalPages - 1)
    }

    private fun prepareData(
        history: List<DailyActivityEntity>,
        type: FatLessHistoryType,
        offset: Int
    ): Pair<List<HistoryBarModel>, List<NutritionBarModel>> {

        val today = LocalDate.now()
        val monday = today
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(offset.toLong())

        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

        // --- МОКИ ДЛЯ ТЕСТА ---
        val mockSteps = when(offset) {
            -1 -> listOf(11000, 9000, 15000, 12000, 8000, 5000, 6000)
            -2 -> listOf(7000, 7500, 6000, 8000, 10000, 12000, 11000)
            else -> listOf(4841, 12500, 8900, 0, 0, 0, 0)
        }
        val mockCal = listOf(2100, 1800, 2400, 0, 0, 0, 0)

        val stepModels = mutableListOf<HistoryBarModel>()
        val nutritionModels = mutableListOf<NutritionBarModel>()

        weekDays.forEachIndexed { index, date ->
            val dateStr = date.toString()
            val dayData = history.find { it.date == dateStr }
            val isToday = date == today
            val isFuture = date.isAfter(today)
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                .replaceFirstChar { it.uppercase() }

            val stepsValue = if (isFuture) 0f else (dayData?.steps?.toFloat() ?: mockSteps[index].toFloat())

            stepModels.add(HistoryBarModel(
                label = dayLabel,
                value = stepsValue,
                goal = settings.stepGoal.toFloat(),
                isToday = isToday,
                isFuture = isFuture,
                barColor = if (isToday) AppOrange else AppPrimary,
                showStar = stepsValue >= settings.stepGoal && stepsValue > 0
            ))

            val calValue = if (isFuture) 0 else (dayData?.calories ?: mockCal[index])

            nutritionModels.add(NutritionBarModel(
                dayLabel = dayLabel,
                proteins = if (isFuture) 0f else (dayData?.proteins ?: 85f),
                fats = if (isFuture) 0f else (dayData?.fats ?: 65f),
                carbs = if (isFuture) 0f else (dayData?.carbs ?: 210f),
                totalCalories = calValue,
                isToday = isToday,
                isFuture = isFuture
            ))
        }

        return Pair(stepModels, nutritionModels)
    }
}
