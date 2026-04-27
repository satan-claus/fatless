package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.domain.player.IAudioPlayer
import com.niked.fatless.domain.repository.IWorkoutRepository
import com.niked.fatless.core.sensor.StepTracker
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
    private val audioPlayer: IAudioPlayer,
    private val stepTracker: StepTracker,
    private val settings: AppSettings
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadWorkout()

        // Слушаем шаги от датчика в реальном времени и обновляем стейт
        viewModelScope.launch {
            stepTracker.intervalSteps.collect { steps ->
                _uiState.update { it.copy(currentIntervalSteps = steps) }

                // ЛОГИКА АВТОМАТИКИ
                val currentInterval = _uiState.value.workout?.intervals?.getOrNull(_uiState.value.currentIntervalIndex)

                // Если у интервала есть цель по шагам (допустим, мы добавим поле stepGoal)
                if (currentInterval?.trackSteps == true && currentInterval.reps != null) {

                    // Проверяем: достигли цели?
                    if (steps >= currentInterval.reps) {

                        // Читаем общую настройку: рубить сразу или дать добегать время?
                        if (settings.autoFinishOnGoal) {
                            nextInterval() // Бац! Авто-переключение
                        }
                    }
                }
            }
        }
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
            is WorkoutState.COMPLETED -> { }
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(status = WorkoutState.RUNNING) }

        // Если в текущем интервале нужно считать шаги — запускаем датчик
        val currentInterval = _uiState.value.workout?.intervals?.getOrNull(_uiState.value.currentIntervalIndex)
        if (currentInterval?.trackSteps == true) {
            stepTracker.startSession()
        }

        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.status is WorkoutState.RUNNING) {
                delay(1000L)

                if (_uiState.value.timeLeft > 0) {
                    _uiState.update { it.copy(timeLeft = _uiState.value.timeLeft - 1) }

                    // Звуковой отсчет 3, 2, 1
                    if (_uiState.value.timeLeft in 1..3) {
                        audioPlayer.playTick()
                    }

                    if (_uiState.value.timeLeft == 0) {
                        switchToNextInterval()
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        // На паузе шагомер тоже должен замереть
        stepTracker.stopSession()
        _uiState.update { it.copy(status = WorkoutState.PAUSED) }
    }

    fun nextInterval() {
        val wasRunning = _uiState.value.status is WorkoutState.RUNNING
        timerJob?.cancel()
        switchToNextInterval()
        if (wasRunning && _uiState.value.status !is WorkoutState.COMPLETED) {
            startTimer()
        }
    }

    private fun switchToNextInterval() {
        // 1. Гасим датчик
        stepTracker.stopSession()
        // 2. СБРАСЫВАЕМ счетчик в трекере до нуля для следующего круга
        stepTracker.resetSession()

        val state = _uiState.value
        val nextIndex = state.currentIntervalIndex + 1
        val intervals = state.workout?.intervals ?: return

        if (nextIndex < intervals.size) {
            audioPlayer.playNext()
            _uiState.update { it.copy(
                currentIntervalIndex = nextIndex,
                timeLeft = intervals[nextIndex].seconds,
                // currentIntervalSteps = 0 больше не нужно писать вручную,
                // оно прилетит из stepTracker.resetSession() через collect
            ) }
        } else {
            audioPlayer.playFinish()
            _uiState.update { it.copy(
                status = WorkoutState.COMPLETED,
                timeLeft = 0
            ) }
            timerJob?.cancel()
        }
    }

    fun resetWorkout() {
        // 1. Убиваем таймер
        timerJob?.cancel()

        // 2. ОСТАНАВЛИВАЕМ И СБРАСЫВАЕМ ШАГОМЕР
        stepTracker.stopSession()
        stepTracker.resetSession()

        // 3. Откатываем всё к первому интервалу
        val firstIntervalSeconds = _uiState.value.workout?.intervals?.firstOrNull()?.seconds ?: 0
        _uiState.update { it.copy(
            currentIntervalIndex = 0,
            timeLeft = firstIntervalSeconds,
            currentIntervalSteps = 0, // Зачищаем UI
            status = WorkoutState.READY
        ) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stepTracker.stopSession()
        audioPlayer.release()
    }
}

data class WorkoutUiState(
    val workout: Workout? = null,
    val currentIntervalIndex: Int = 0,
    val timeLeft: Int = 0,
    val currentIntervalSteps: Int = 0, // Поле для отображения шагов на карточке
    val status: WorkoutState = WorkoutState.READY
)
