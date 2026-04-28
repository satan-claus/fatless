package com.niked.fatless.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.core.sensor.StepTracker
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.INutritionRepository
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val nutritionRepository: INutritionRepository,
    private val settings: AppSettings,
    private val stepTracker: StepTracker
) : ViewModel() {

    // 1. Тренировки
    val workouts: StateFlow<List<Workout>> = workoutRepository.observeAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 2. Питание
    val todayNutrition: StateFlow<NutritionUiState> = nutritionRepository.getDiaryForToday()
        .map { entries ->
            NutritionUiState(
                totalProteins = entries.sumOf { it.totalProteins.toDouble() }.toFloat(),
                totalFats = entries.sumOf { it.totalFats.toDouble() }.toFloat(),
                totalCarbs = entries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
                totalCalories = entries.sumOf { it.totalCalories }
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, NutritionUiState())

    // 3. ЖИВЫЕ ШАГИ
    private val _steps = MutableStateFlow(settings.todaySteps)
    val steps = _steps.asStateFlow()

    val stepGoal = settings.stepGoal

    // Храним слушателя как поле класса, чтобы GC его не сожрал
    private var stepsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        // Регистрируем слушателя в SharedPreferences через наш AppSettings
        stepsListener = settings.observeSteps { newSteps ->
            _steps.value = newSteps
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Обязательно отписываемся, чтобы не было утечек памяти
        stepsListener?.let { settings.unregisterListener(it) }
    }
}