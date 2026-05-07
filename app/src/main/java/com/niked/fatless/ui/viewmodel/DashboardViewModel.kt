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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

    val currentDate = flow {
        while(true) {
            emit(LocalDate.now().toString())
            delay(60000) // Проверяем раз в минуту
        }
    }.distinctUntilChanged()

    // 1. Тренировки
    val workouts: StateFlow<List<Workout>> = workoutRepository.observeAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 2. Питание
    @OptIn(ExperimentalCoroutinesApi::class)
    val todayNutrition: StateFlow<NutritionUiState> = currentDate
        .flatMapLatest { date ->
            // Теперь мы просим данные для КОНКРЕТНОЙ даты,
            // которую выдал наш таймер currentDate
            nutritionRepository.getDiaryForToday(date)
        }
        .map { entries ->
            NutritionUiState(
                totalProteins = entries.sumOf { it.totalProteins.toDouble() }.toFloat(),
                totalFats = entries.sumOf { it.totalFats.toDouble() }.toFloat(),
                totalCarbs = entries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
                totalCalories = entries.sumOf { it.totalCalories.toDouble() }.toFloat()
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, NutritionUiState())

    // 3. ЖИВЫЕ ШАГИ
    private val _steps = MutableStateFlow(settingsRepository.todaySteps)
    val steps = _steps.asStateFlow()

    val stepGoal = settingsRepository.stepGoal

    val historyState = activityRepository.getActivityHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val distanceKm: StateFlow<Float> = steps.map { currentSteps ->
        val strideInCm = settingsRepository.userHeight * 0.415f
        // см -> км
        (currentSteps * strideInCm) / 100_000f
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0f
    )

    // Храним слушателя как поле класса, чтобы GC его не сожрал
    private var stepsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    // Загружаем типы упражнений из БД
    val exerciseTypes = exerciseRepository.getExerciseTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Стейт для выбранного ID (по умолчанию ходьба)
    private val _selectedExerciseId = MutableStateFlow("walk")

    // Комбинируем список и ID в один объект ExerciseType
    val selectedExercise = combine(exerciseTypes, _selectedExerciseId) { types, id ->
        types.find { it.id == id } ?: types.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onExerciseTypeSelected(id: String) {
        _selectedExerciseId.value = id
    }

    // УМНЫЙ РАСЧЕТ Сожженных калорий
    // Калории = Шаги * Вес * (MET / 7000)
    // Коэффициент 7000 — усредненный множитель интенсивности для шагов
    val burnedCalories = combine(
        steps, // Твой Flow шагов
        selectedExercise,
        // Оборачиваем вес в Flow, чтобы пересчитывать при смене веса в настройках
        flowOf(settingsRepository.userWeight)
    ) { currentSteps, activity, weight ->
        val met = activity?.metValue ?: 3.5f
        val userWeight = if (weight > 0) weight.toFloat() else 75f

        currentSteps * userWeight * (met / 7000f)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)


    init {
        // Регистрируем слушателя в SharedPreferences через AppSettings
        stepsListener = settingsRepository.observeSteps { newSteps ->
            _steps.value = newSteps
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Обязательно отписываемся, чтобы не было утечек памяти
        stepsListener?.let { settingsRepository.unregisterListener(it) }
    }
}