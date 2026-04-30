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

    // Офсет для синхронизации заголовка (дат)
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    val allHistory = repository.getActivityHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    // ДИАПАЗОН ДАТ (Слушает офсет от пейджера)
    val weekRange: StateFlow<String> = _weekOffset.map { offset ->
        val today = LocalDate.now()
        val monday = today
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(offset.toLong())
        val sunday = monday.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))
        "${monday.format(formatter)} — ${sunday.format(formatter)}"
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    // КОЛИЧЕСТВО СТРАНИЦ
    val pageCount = repository.getActivityHistory().map { history ->
        if (history.isEmpty()) return@map 3
        val firstDate = LocalDate.parse(history.minOf { it.date })
        val today = LocalDate.now()
        val mondayOfFirstWeek = firstDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeksBetween = ChronoUnit.WEEKS.between(mondayOfFirstWeek, today).toInt()
        (weeksBetween + 1 + 2).coerceAtLeast(3)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 3)

    fun setHistoryType(type: FatLessHistoryType) {
        _historyType.value = type
    }

    // Получение данных для конкретной страницы пейджера
    fun getWeekData(page: Int, totalPages: Int, history: List<DailyActivityEntity>): Pair<List<HistoryBarModel>, List<NutritionBarModel>> {
        val offset = page - (totalPages - 1)
        return prepareData(history, offset)
    }

    // Обновление офсета (вызывается из LaunchedEffect в UI при свайпе)
    fun updateOffsetFromPage(page: Int, totalPages: Int) {
        _weekOffset.value = page - (totalPages - 1)
    }

    private fun prepareData(
        history: List<DailyActivityEntity>,
        offset: Int
    ): Pair<List<HistoryBarModel>, List<NutritionBarModel>> {

        val today = LocalDate.now()
        val monday = today
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(offset.toLong())

        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

        // --- МОКИ ---
        val mockSteps = when(offset) {
            -1 -> listOf(11000f, 9000f, 15000f, 12000f, 8000f, 5000f, 6000f)
            -2 -> listOf(7000f, 7500f, 6000f, 8000f, 10000f, 12000f, 11000f)
            else -> listOf(4841f, 12500f, 8900f, 0f, 0f, 0f, 0f)
        }
        val mockCal = when(offset) {
            -1 -> listOf(0, 0, 0, 0, 0, 0, 0)
            -2 -> listOf(0, 0, 0, 0, 0, 0, 0)
            else -> listOf(2100, 1800, 2400, 0, 0, 0, 0)
        }

        val stepModels = mutableListOf<HistoryBarModel>()
        val nutritionModels = mutableListOf<NutritionBarModel>()

        weekDays.forEachIndexed { index, date ->
            val dateStr = date.toString()
            val dayData = history.find { it.date == dateStr }
            val isToday = date == today
            val isFuture = date.isAfter(today)
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                .replaceFirstChar { it.uppercase() }

            val stepsValue = when {
                isFuture -> 0f
                isToday -> settings.todaySteps.toFloat()
                else -> dayData?.steps?.toFloat() ?: mockSteps[index]
            }

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
                proteins = dayData?.proteins ?: 0f,
                fats = dayData?.fats ?: 0f,
                carbs = dayData?.carbs ?: 0f,
                totalCalories = calValue,
                isToday = isToday,
                isFuture = isFuture
            ))
        }

        return Pair(stepModels, nutritionModels)
    }
}
