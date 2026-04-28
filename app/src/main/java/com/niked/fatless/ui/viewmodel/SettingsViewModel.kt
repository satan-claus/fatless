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
            autoFinishOnGoal = settings.autoFinishOnGoal,
            stepGoal = settings.stepGoal
        ) }
    }

    fun toggleSound(enabled: Boolean) {
        settings.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun toggleAutoFinish(enabled: Boolean) {
        settings.autoFinishOnGoal = enabled
        _uiState.update { it.copy(autoFinishOnGoal = enabled) }
    }

    fun updateStepGoal(newGoal: Int) {
        settings.stepGoal = newGoal
        _uiState.update { it.copy(stepGoal = newGoal) }
    }
}

data class SettingsUiState(
    val isSoundEnabled: Boolean = true,
    val autoFinishOnGoal: Boolean = false,
    val stepGoal: Int = 10000
)
