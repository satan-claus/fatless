package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.*
import com.niked.fatless.domain.repository.IExerciseRepository
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkoutCreateViewModel @Inject constructor(
    private val exerciseRepository: IExerciseRepository,
    private val workoutRepository: IWorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutCreateUiState())
    val uiState = _uiState.asStateFlow()

    // 1. Загружаем типы из справочника при старте
    init {
        viewModelScope.launch {
            exerciseRepository.getExerciseTypes().collect { types ->
                _uiState.update { it.copy(exerciseTypes = types) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun addInterval() {
        val lastInterval = _uiState.value.intervals.lastOrNull()
        if (lastInterval != null && lastInterval.name.isBlank()) return

        // По умолчанию для нового интервала ставим тип WORK (Работа)
        val newInterval = Interval(name = "", seconds = 30, type = IntervalType.WORK)
        _uiState.update { it.copy(intervals = it.intervals + newInterval) }
    }

    fun updateInterval(
        index: Int,
        name: String,
        seconds: Int,
        reps: Int?,
        trackSteps: Boolean
    ) {
        _uiState.update { state ->
            val newList = state.intervals.toMutableList()
            if (index in newList.indices) {
                val currentInterval = newList[index]

                // УМНАЯ ЛОГИКА: если юзер включил "Шаги", но тип упражнения не выбран,
                // автоматически ставим "Ходьбу" (первый элемент из справочника)
                val defaultType = if (trackSteps && currentInterval.exerciseType == null) {
                    state.exerciseTypes.firstOrNull { it.id == "walk" } ?: state.exerciseTypes.firstOrNull()
                } else if (!trackSteps) {
                    // Если шаги выключили — очищаем тип, чтобы не жечь лишнего
                    null
                } else {
                    currentInterval.exerciseType
                }

                newList[index] = currentInterval.copy(
                    name = name,
                    seconds = seconds,
                    reps = reps,
                    trackSteps = trackSteps,
                    exerciseType = defaultType
                )
            }
            state.copy(intervals = newList)
        }
    }

    // 2. Смена конкретно типа упражнения (выбор из селектора)
    fun updateIntervalExerciseType(intervalId: String, type: ExerciseType) {
        _uiState.update { state ->
            state.copy(
                intervals = state.intervals.map {
                    if (it.id == intervalId) it.copy(exerciseType = type) else it
                }
            )
        }
    }

    fun removeInterval(index: Int) {
        _uiState.update { state ->
            val newList = state.intervals.toMutableList()
            if (newList.size > 1) {
                newList.removeAt(index)
                state.copy(intervals = newList)
            } else state
        }
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value
        val canSave = state.title.isNotBlank() &&
            state.intervals.all { it.name.isNotBlank() }

        if (!canSave || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val workout = Workout(
                // Генерим ID для новой тренировки
                id = UUID.randomUUID().toString(),
                title = state.title,
                intervals = state.intervals
            )
            workoutRepository.saveWorkout(workout)
            onSuccess()
        }
    }
}

data class WorkoutCreateUiState(
    val title: String = "",
    val intervals: List<Interval> = listOf(
        Interval(name = "", seconds = 30, type = IntervalType.PREPARATION)
    ),
    // Сюда прилетят Ходьба/Бег/Спринт из базы
    val exerciseTypes: List<ExerciseType> = emptyList(),
    val isSaving: Boolean = false
)

