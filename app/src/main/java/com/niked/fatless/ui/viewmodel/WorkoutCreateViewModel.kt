package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.IntervalType
import com.niked.fatless.domain.model.Workout
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

    // В скрине мы обращаемся к uiState, давай назовем его так
    private val _uiState = MutableStateFlow(WorkoutCreateUiState())
    val uiState = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun addInterval() {
        val newInterval = Interval("Новый интервал", 30, IntervalType.WORK)
        _uiState.update { it.copy(intervals = it.intervals + newInterval) }
    }

    // В скрине вызывается updateInterval(index, name, seconds)
    fun updateInterval(index: Int, name: String, seconds: Int) {
        val newList = _uiState.value.intervals.toMutableList()
        if (index in newList.indices) {
            val updated = newList[index].copy(name = name, seconds = seconds)
            newList[index] = updated
            _uiState.update { it.copy(intervals = newList) }
        }
    }

    // В скрине вызывается removeInterval
    fun removeInterval(index: Int) {
        val newList = _uiState.value.intervals.toMutableList()
        if (newList.size > 1) {
            newList.removeAt(index)
            _uiState.update { it.copy(intervals = newList) }
        }
    }

    // В скрине вызывается saveWorkout
    fun saveWorkout(onSuccess: () -> Unit) {
        if (_uiState.value.title.isBlank() || _uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val workout = Workout(
                title = _uiState.value.title,
                intervals = _uiState.value.intervals
            )
            repository.saveWorkout(workout)
            onSuccess()
        }
    }
}

data class WorkoutCreateUiState(
    val title: String = "",
    val intervals: List<Interval> = listOf(
        Interval("Подготовка", 10, IntervalType.PREPARATION)
    ),
    val isSaving: Boolean = false
)
