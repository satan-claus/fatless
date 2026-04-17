package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.domain.player.IAudioPlayer
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: IWorkoutRepository,
    savedStateHandle: SavedStateHandle,
    private val audioPlayer: IAudioPlayer
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            repository.getWorkoutById(workoutId)?.let { workout ->
                _uiState.update { it.copy(
                    workout = workout,
                    timeLeft = workout.intervals.firstOrNull()?.seconds ?: 0
                ) }
            }
        }
    }

    fun toggleTimer() {
        when (_uiState.value.status) {
            is WorkoutState.READY, is WorkoutState.PAUSED -> startTimer()
            is WorkoutState.RUNNING -> pauseTimer()
            is WorkoutState.COMPLETED -> {  }
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(status = WorkoutState.RUNNING) }
        timerJob = viewModelScope.launch {
            // Добавляем проверку, что мы все еще в состоянии RUNNING
            while (isActive && _uiState.value.status is WorkoutState.RUNNING) {
                delay(1000L)
                val currentState = _uiState.value

                if (currentState.timeLeft > 0) {
                    _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }

                    // Пищим на 3, 2, 1 секундах
                    if (_uiState.value.timeLeft in 1..3) {
                        audioPlayer.playTick()
                    }

                    // Переключаем, когда дотикали до нуля
                    if (_uiState.value.timeLeft == 0) {
                        switchToNextInterval()
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(status = WorkoutState.PAUSED) }
    }

    private fun switchToNextInterval() {
        val state = _uiState.value
        val nextIndex = state.currentIntervalIndex + 1
        val intervals = state.workout?.intervals ?: return

        if (nextIndex < intervals.size) {
            // Переход на следующий круг
            audioPlayer.playNext()
            _uiState.update { it.copy(
                currentIntervalIndex = nextIndex,
                timeLeft = intervals[nextIndex].seconds
            ) }
        } else {
            // ПОБЕДА!
            timerJob?.cancel()
            audioPlayer.playFinish()
            _uiState.update { it.copy(status = WorkoutState.COMPLETED) }
        }
    }

    fun resetWorkout() {
        // 1. Останавливаем таймер, если он тикал
        timerJob?.cancel()

        // 2. Откатываем стейт к началу
        val firstIntervalSeconds = _uiState.value.workout?.intervals?.firstOrNull()?.seconds ?: 0

        _uiState.update { it.copy(
            currentIntervalIndex = 0,
            timeLeft = firstIntervalSeconds,
            status = WorkoutState.READY
        ) }
    }

}

data class WorkoutUiState(
    val workout: Workout? = null,
    val currentIntervalIndex: Int = 0,
    val timeLeft: Int = 0,
    val status: WorkoutState = WorkoutState.READY
)