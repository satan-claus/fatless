package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val repository: IWorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutListUiState>(WorkoutListUiState.Loading)
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()

    init {
        observeWorkouts()
    }

    private fun observeWorkouts() {
        viewModelScope.launch {
            // Инициализируем дефолты
            repository.initializeDefaultData()

            // Подписываемся на реактивный поток из БД
            repository.observeAllWorkouts().collect { workouts ->
                _uiState.value = WorkoutListUiState.Success(workouts)
            }
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }
}

sealed class WorkoutListUiState {
    object Loading : WorkoutListUiState()
    data class Success(val workouts: List<Workout>) : WorkoutListUiState()
    data class Error(val message: String) : WorkoutListUiState()
}