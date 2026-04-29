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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.find

@HiltViewModel
class FatLessHistoryViewModel @Inject constructor(
    private val activityRepository: IActivityRepository,
    private val settings: AppSettings
) : ViewModel() {

    private val _historyType = MutableStateFlow(FatLessHistoryType.STEPS)
    val historyType = _historyType.asStateFlow()

    // Основной поток данных для графика
    val chartData = combine(
        activityRepository.getActivityHistory(),
        _historyType
    ) { history, type ->
        prepareData(history, type)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList<HistoryBarModel>(), emptyList<NutritionBarModel>()))

    fun setHistoryType(type: FatLessHistoryType) {
        _historyType.value = type
    }

    private fun prepareData(history: List<DailyActivityEntity>, type: FatLessHistoryType): Pair<List<HistoryBarModel>, List<NutritionBarModel>> {
        val today = LocalDate.now()
        val last7Days = (0..6).reversed().map { today.minusDays(it.toLong()) }

        val stepModels = mutableListOf<HistoryBarModel>()
        val nutritionModels = mutableListOf<NutritionBarModel>()

        last7Days.forEach { date ->
            val dateStr = date.toString() // ГГГГ-ММ-ДД
            val dayData = history.find { it.date == dateStr }
            val isToday = date == today
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")).replaceFirstChar { it.uppercase() }

            // Готовим модель для шагов
            stepModels.add(HistoryBarModel(
                label = dayLabel,
                value = dayData?.steps?.toFloat() ?: 0f,
                goal = settings.stepGoal.toFloat(),
                isToday = isToday,
                barColor = if (isToday) AppOrange else AppPrimary,
                showStar = (dayData?.steps ?: 0) >= settings.stepGoal && (dayData?.steps ?: 0) > 0
            ))

            // Готовим модель для питания
            nutritionModels.add(NutritionBarModel(
                dayLabel = dayLabel,
                proteins = dayData?.proteins?.toFloat() ?: 0f,
                fats = dayData?.fats?.toFloat() ?: 0f,
                carbs = dayData?.carbs?.toFloat() ?: 0f,
                totalCalories = dayData?.calories ?: 0,
                isToday = isToday
            ))
        }

        return Pair(stepModels, nutritionModels)
    }
}
