package com.niked.fatless.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.IExerciseRepository
import com.niked.fatless.domain.repository.INutritionRepository
import com.niked.fatless.domain.repository.ISettingsRepository
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: IActivityRepository,
    private val exerciseRepository: IExerciseRepository,
    private val nutritionRepository: INutritionRepository,
    private val settingsRepository: ISettingsRepository,
    private val workoutRepository: IWorkoutRepository,
) : ViewModel() {

    // Таймер даты
    val currentDate = flow {
        while(true) {
            emit(LocalDate.now().toString())
            delay(60000)
        }
    }.distinctUntilChanged()

    // 1. ТРЕНИРОВКИ
    val workouts: StateFlow<List<Workout>> = workoutRepository.observeAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 2. ПИТАНИЕ
    @OptIn(ExperimentalCoroutinesApi::class)
    val todayNutrition: StateFlow<NutritionUiState> = currentDate
        .flatMapLatest { date -> nutritionRepository.getDiaryForToday(date) }
        .map { entries ->
            NutritionUiState(
                totalProteins = entries.sumOf { it.totalProteins.toDouble() }.toFloat(),
                totalFats = entries.sumOf { it.totalFats.toDouble() }.toFloat(),
                totalCarbs = entries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
                totalCalories = entries.sumOf { it.totalCalories.toDouble() }.toFloat()
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, NutritionUiState())

    // 3. ЖИВЫЕ ДАННЫЕ (из SharedPreferences через слушателя)
    private val _steps = MutableStateFlow(settingsRepository.todaySteps)
    val steps = _steps.asStateFlow()

    private val _burnedCalories = MutableStateFlow(settingsRepository.todayBurnedCalories)
    val burnedCalories = _burnedCalories.asStateFlow()

    // 4. СПРАВОЧНИК УПРАЖНЕНИЙ (Для UI, если он нужен на Dashboard)
    val exerciseTypes = exerciseRepository.getExerciseTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 5. ОСТАЛЬНОЕ
    val stepGoal = settingsRepository.stepGoal
    val historyState = activityRepository.getActivityHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val distanceKm: StateFlow<Float> = steps.map { currentSteps ->
        val strideInCm = settingsRepository.userHeight * 0.415f
        (currentSteps * strideInCm) / 100_000f
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        // Слушаем изменения в префсах, которые валит StepService
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                "pref_today_steps" -> {
                    _steps.value = prefs.getInt(key, 0)
                }
                "pref_today_burned_calories" -> {
                    _burnedCalories.value = prefs.getFloat(key, 0f)
                }
            }
        }
        settingsRepository.registerListener(prefsListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        prefsListener?.let { settingsRepository.unregisterListener(it) }
    }
}

