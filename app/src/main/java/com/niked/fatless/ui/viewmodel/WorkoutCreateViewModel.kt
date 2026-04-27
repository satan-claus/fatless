package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.*
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutCreateViewModel @Inject constructor(
    private val repository: IWorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutCreateUiState())
    val uiState = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun addInterval() {
        // БЛОКИРОВКА: не добавляем новый, пока последний пустой
        val lastInterval = _uiState.value.intervals.lastOrNull()
        if (lastInterval != null && lastInterval.name.isBlank()) return

        val newInterval = Interval(name = "", seconds = 30, type = IntervalType.WORK)
        _uiState.update { it.copy(intervals = it.intervals + newInterval) }
    }

    fun updateInterval(index: Int, name: String, seconds: Int, reps: Int?, trackSteps: Boolean) {
        _uiState.update { state ->
            val newList = state.intervals.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(
                    name = name,
                    seconds = seconds,
                    reps = reps,
                    trackSteps = trackSteps
                )
            }
            state.copy(intervals = newList)
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
        val canSave = state.title.isNotBlank() && state.intervals.all { it.name.isNotBlank() }

        if (!canSave || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val workout = Workout(title = state.title, intervals = state.intervals)
            repository.saveWorkout(workout)
            onSuccess()
        }
    }
}

data class WorkoutCreateUiState(
    val title: String = "",
    val intervals: List<Interval> = listOf(Interval(name = "", seconds = 30, type = IntervalType.PREPARATION)),
    val isSaving: Boolean = false
)
