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
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FatLessHistoryViewModel @Inject constructor(
    private val repository: IActivityRepository,
    private val settings: AppSettings
) : ViewModel() {

    private val _historyType = MutableStateFlow(FatLessHistoryType.STEPS)
    val historyType: StateFlow<FatLessHistoryType> = _historyType.asStateFlow()

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

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

        // --- 🎯 НАШИ ТЕСТОВЫЕ МОКИ ---
        val mockSteps = listOf(4841, 12500, 8900, 0, 0, 0, 0)
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

            // Логика для ШАГОВ
            // Если в базе пусто и это не будущее - суем мок
            val stepsValue = if (isFuture) 0f
            else (dayData?.steps?.toFloat() ?: mockSteps[index].toFloat())

            stepModels.add(HistoryBarModel(
                label = dayLabel,
                value = stepsValue,
                goal = settings.stepGoal.toFloat(),
                isToday = isToday,
                isFuture = isFuture,
                barColor = if (isToday) AppOrange else AppPrimary,
                showStar = stepsValue >= settings.stepGoal && stepsValue > 0
            ))

            // Логика для ПИТАНИЯ
            val calValue = if (isFuture) 0
            else (dayData?.calories ?: mockCal[index])

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
