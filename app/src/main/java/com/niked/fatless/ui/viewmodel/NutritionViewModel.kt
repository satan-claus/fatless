package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class NutritionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState = _uiState.asStateFlow()

    fun updateProteins(value: Float) = _uiState.update { it.copy(proteins = value) }
    fun updateFats(value: Float) = _uiState.update { it.copy(fats = value) }
    fun updateCarbs(value: Float) = _uiState.update { it.copy(carbs = value) }

    // Калории считаем на лету: 1г белка/углей = 4ккал, 1г жира = 9ккал
    fun calculateCalories(): Int {
        val s = _uiState.value
        return ((s.proteins * 4) + (s.carbs * 4) + (s.fats * 9)).toInt()
    }
}

data class NutritionUiState(
    val proteins: Float = 0f,
    val fats: Float = 0f,
    val carbs: Float = 0f
)
