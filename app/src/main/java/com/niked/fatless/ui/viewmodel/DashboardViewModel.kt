package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.sensor.StepTracker
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.INutritionRepository
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val nutritionRepository: INutritionRepository,
    private val stepTracker: StepTracker
) : ViewModel() {

    // 1. Стрим тренировок
    val workouts: StateFlow<List<Workout>> = workoutRepository.observeAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 2. Стрим питания (КБЖУ) за сегодня
    val todayNutrition: StateFlow<NutritionUiState> = nutritionRepository.getDiaryForToday()
        .map { entries ->
            NutritionUiState(
                totalProteins = entries.sumOf { it.totalProteins.toDouble() }.toFloat(),
                totalFats = entries.sumOf { it.totalFats.toDouble() }.toFloat(),
                totalCarbs = entries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
                totalCalories = entries.sumOf { it.totalCalories }
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, NutritionUiState())

    // 3. ЖИВЫЕ ШАГИ (Берем из трекера)
    // Но для Дашборда нам нужны суточные шаги.
    // Пока подпишемся на интервальные, чтобы просто увидеть, что связь есть.
    val steps: StateFlow<Int> = stepTracker.intervalSteps
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
}