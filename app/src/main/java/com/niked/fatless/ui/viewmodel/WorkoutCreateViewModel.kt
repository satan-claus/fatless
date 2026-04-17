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

    private val _state = MutableStateFlow(WorkoutCreateState())
    val state = _state.asStateFlow()

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun addInterval() {
        val newInterval = Interval("Новый интервал", 30, IntervalType.WORK)
        _state.update { it.copy(intervals = it.intervals + newInterval) }
    }

    fun updateInterval(index: Int, interval: Interval) {
        val newList = _state.value.intervals.toMutableList()
        if (index in newList.indices) {
            newList[index] = interval
            _state.update { it.copy(intervals = newList) }
        }
    }

    fun deleteInterval(index: Int) {
        val newList = _state.value.intervals.toMutableList()
        if (newList.size > 1) {
            newList.removeAt(index)
            _state.update { it.copy(intervals = newList) }
        }
    }

    fun save(onSuccess: () -> Unit) {
        if (_state.value.title.isBlank()) return
        viewModelScope.launch {
            val workout = Workout(
                title = _state.value.title,
                intervals = _state.value.intervals
            )
            repository.saveWorkout(workout)
            onSuccess()
        }
    }
}

data class WorkoutCreateState(
    val title: String = "",
    val intervals: List<Interval> = listOf(Interval("Подготовка", 10, IntervalType.PREPARATION))
)