package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.niked.fatless.domain.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Загружаем текущие значения
        _uiState.update { it.copy(
            isSoundEnabled = settingsRepository.isSoundEnabled,
            soundVolume = settingsRepository.soundVolume,
            autoFinishOnGoal = settingsRepository.autoFinishOnGoal,
            stepGoal = settingsRepository.stepGoal,
            userHeight = settingsRepository.userHeight,
            userWeight = settingsRepository.userWeight
        ) }
    }

    fun isFirstLaunch(): Boolean = settingsRepository.isFirstLaunch

    fun setFirstLaunchDone() {
        settingsRepository.isFirstLaunch = false
    }

    fun toggleSound(enabled: Boolean) {
        settingsRepository.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun updateVolume(newVolume: Float) {
        _uiState.update { it.copy(soundVolume = newVolume) }
        settingsRepository.soundVolume = newVolume
    }

    fun toggleAutoFinish(enabled: Boolean) {
        settingsRepository.autoFinishOnGoal = enabled
        _uiState.update { it.copy(autoFinishOnGoal = enabled) }
    }

    fun updateStepGoal(newGoal: Int) {
        settingsRepository.stepGoal = newGoal
        _uiState.update { it.copy(stepGoal = newGoal) }
    }

    fun updateHeight(newHeight: Int) {
        settingsRepository.userHeight = newHeight
        _uiState.update { it.copy(userHeight = newHeight) }
    }

    fun updateWeight(newWeight: Int) {
        settingsRepository.userWeight = newWeight
        _uiState.update { it.copy(userWeight = newWeight) }
    }
}

data class SettingsUiState(
    val isSoundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val stepGoal: Int = 10000,
    val userHeight: Int = 175,
    val userWeight: Int = 75,
    val autoFinishOnGoal: Boolean = false
)
