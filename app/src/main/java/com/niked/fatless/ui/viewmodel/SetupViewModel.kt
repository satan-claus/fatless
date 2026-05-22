package com.niked.fatless.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    private val activityRepository: IActivityRepository
) : ViewModel() {

    var height by mutableStateOf("")
    var weight by mutableStateOf("")

    fun saveProfile(onSuccess: () -> Unit) {
        val h = height.toIntOrNull() ?: return
        val w = weight.toFloatOrNull() ?: return

        settingsRepository.userHeight = h
        settingsRepository.userWeight = w

        viewModelScope.launch {
            // Фиксируем первую точку в истории веса
            activityRepository.saveWeight(LocalDate.now().toString(), w)
            // Помечаем, что первый запуск пройден
            settingsRepository.isFirstLaunch = false
            onSuccess()
        }
    }
}