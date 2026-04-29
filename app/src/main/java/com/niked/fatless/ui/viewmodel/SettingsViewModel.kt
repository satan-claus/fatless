package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.niked.fatless.core.data.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Загружаем текущие значения
        _uiState.update { it.copy(
            isSoundEnabled = settings.isSoundEnabled,
            soundVolume = settings.soundVolume,
            autoFinishOnGoal = settings.autoFinishOnGoal,
            stepGoal = settings.stepGoal,
            userHeight = settings.userHeight,
            userWeight = settings.userWeight
        ) }
    }

    fun isFirstLaunch(): Boolean = settings.isFirstLaunch

    fun setFirstLaunchDone() {
        settings.isFirstLaunch = false
    }

    fun toggleSound(enabled: Boolean) {
        settings.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun updateVolume(newVolume: Float) {
        _uiState.update { it.copy(soundVolume = newVolume) }
        settings.soundVolume = newVolume
    }

    fun toggleAutoFinish(enabled: Boolean) {
        settings.autoFinishOnGoal = enabled
        _uiState.update { it.copy(autoFinishOnGoal = enabled) }
    }

    fun updateStepGoal(newGoal: Int) {
        settings.stepGoal = newGoal
        _uiState.update { it.copy(stepGoal = newGoal) }
    }

    fun updateHeight(newHeight: Int) {
        settings.userHeight = newHeight
        _uiState.update { it.copy(userHeight = newHeight) }
    }

    fun updateWeight(newWeight: Int) {
        settings.userWeight = newWeight
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
