package com.niked.fatless.ui.component.fatlesshistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.ISettingsRepository
import com.niked.fatless.ui.theme.ColorSteps
import com.niked.fatless.ui.theme.ColorStepsToday
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
    private val activityRepository: IActivityRepository,
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _historyType = MutableStateFlow(FatLessHistoryType.STEPS)
    val historyType: StateFlow<FatLessHistoryType> = _historyType.asStateFlow()

    // Офсет для синхронизации заголовка (дат)
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    val allHistory = activityRepository.getActivityHistory()
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
    val pageCount = allHistory.map { historyList ->
        if (historyList.isEmpty()) 1
        else {
            // 1. Берем самую раннюю дату из базы
            val earliestDate = historyList.minOf { LocalDate.parse(it.date) }

            // 2. Находим понедельники для обеих дат
            val earliestMonday = earliestDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            // 3. Считаем разницу между понедельниками в неделях
            val weeksBetween = ChronoUnit.WEEKS.between(earliestMonday, currentMonday)

            // 4. Страниц всегда на одну больше, чем разница
            (weeksBetween + 1).toInt().coerceAtLeast(1)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 1)

    fun setHistoryType(type: FatLessHistoryType) {
        _historyType.value = type
    }

    // Получение данных для конкретной страницы пейджера
    fun getWeekData(page: Int, totalPages: Int, history: List<DailyActivity>): Pair<List<HistoryBarModel>, List<NutritionBarModel>> {
        val offset = page - (totalPages - 1)
        return prepareData(history, offset)
    }

    // Обновление офсета (вызывается из LaunchedEffect в UI при свайпе)
    fun updateOffsetFromPage(page: Int, totalPages: Int) {
        _weekOffset.value = page - (totalPages - 1)
    }

    private fun prepareData(
        history: List<DailyActivity>,
        offset: Int
    ): Pair<List<HistoryBarModel>, List<NutritionBarModel>> {

        val today = LocalDate.now()
        val monday = today
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(offset.toLong())

        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

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
                isToday -> settingsRepository.todaySteps.toFloat()
                // Если данных нет, возвращаем 0f
                else -> dayData?.steps?.toFloat() ?: 0f
            }

            stepModels.add(HistoryBarModel(
                label = dayLabel,
                value = stepsValue,
                goal = settingsRepository.stepGoal.toFloat(),
                isToday = isToday,
                isFuture = isFuture,
                barColor = if (isToday) ColorStepsToday else ColorSteps,
                showStar = stepsValue >= settingsRepository.stepGoal && stepsValue > 0
            ))

            val calValue = if (isFuture) 0f else (dayData?.consumedCalories ?: 0f)

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
