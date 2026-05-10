package com.niked.fatless.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.location.TrackingService
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants.ACTION_START_TRACKING
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.core.utils.Constants.ACTION_STOP_TRACKING
import com.niked.fatless.core.utils.Constants.PREF_TODAY_BURNED_CALORIES
import com.niked.fatless.core.utils.Constants.PREF_TODAY_STEPS
import com.niked.fatless.core.utils.Constants.PREF_USER_WEIGHT
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: IActivityRepository,
    private val exerciseRepository: IExerciseRepository,
    private val nutritionRepository: INutritionRepository,
    private val settingsRepository: ISettingsRepository,
    private val workoutRepository: IWorkoutRepository,
    private val logger: AppLogger,
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

    private val _weight = MutableStateFlow(settingsRepository.userWeight)
    val weight = _weight.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        // Слушаем изменения в префсах, которые валит StepService
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                PREF_TODAY_STEPS -> {
                    _steps.value = prefs.getInt(key, 0)
                }
                PREF_TODAY_BURNED_CALORIES -> {
                    _burnedCalories.value = prefs.getFloat(key, 0f)
                }
                PREF_USER_WEIGHT -> {
                    _weight.value = prefs.getFloat(key, 75.0f)
                }
            }
        }
        settingsRepository.registerListener(prefsListener!!)
    }

    fun updateWeight(newWeight: Float) {
        settingsRepository.userWeight = newWeight
        viewModelScope.launch {
            activityRepository.saveWeight(LocalDate.now().toString(), newWeight)
        }
    }

    fun toggleTracking(context: Context) {
        // Проверка перед запуском, чтобы не словить крэш
        val hasFine = ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) {
            // Тут можно либо лог записать, либо тост показать
            logger.log(LogLevel.ERROR, "UI", "Попытка запуска GPS без разрешений")
            return
        }
        val intent = Intent(context, TrackingService::class.java)
        if (_isTracking.value) {
            intent.action = ACTION_STOP_TRACKING
            context.startService(intent)
            _isTracking.value = false
            logger.log(LogLevel.SYSTEM, "UI", "Пользователь остановил трекинг")
        } else {
            intent.action = ACTION_START_TRACKING
            context.startService(intent)
            _isTracking.value = true
            logger.log(LogLevel.SYSTEM, "UI", "Пользователь запустил трекинг")
        }
    }

    override fun onCleared() {
        super.onCleared()
        prefsListener?.let { settingsRepository.unregisterListener(it) }
    }
}

