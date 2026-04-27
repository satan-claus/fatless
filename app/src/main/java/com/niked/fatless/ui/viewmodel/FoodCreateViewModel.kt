package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.MeasureUnit
import com.niked.fatless.domain.repository.INutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FoodCreateViewModel @Inject constructor(
    private val repository: INutritionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Достаем имя, которое прилетело из поиска
    private val initName: String = savedStateHandle["initName"] ?: ""

    private val _uiState = MutableStateFlow(FoodCreateUiState(name = initName))
    val uiState = _uiState.asStateFlow()

    val categories = listOf("Общее", "Мясо", "Овощи", "Фрукты", "Гарниры", "Напитки", "Десерты")

    fun updateName(v: String) = _uiState.update { it.copy(name = v) }
    fun updateProteins(v: String) = _uiState.update { it.copy(proteins = v) }
    fun updateFats(v: String) = _uiState.update { it.copy(fats = v) }
    fun updateCarbs(v: String) = _uiState.update { it.copy(carbs = v) }
    fun updateCalories(v: String) = _uiState.update { it.copy(calories = v) }
    fun updateUnit(v: MeasureUnit) = _uiState.update { it.copy(unit = v) }
    fun updateCategory(v: String) = _uiState.update { it.copy(category = v) }

    fun saveProduct(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) return

        val food = Food(
            id = UUID.randomUUID().toString(),
            name = state.name,
            proteins = state.proteins.replace(",", ".").toFloatOrNull() ?: 0f,
            fats = state.fats.replace(",", ".").toFloatOrNull() ?: 0f,
            carbs = state.carbs.replace(",", ".").toFloatOrNull() ?: 0f,
            calories = state.calories.toIntOrNull() ?: 0,
            category = state.category,
            unit = state.unit,
            isCustom = true
        )

        viewModelScope.launch {
            repository.addProductToLibrary(food)
            onSuccess()
        }
    }
}

data class FoodCreateUiState(
    val name: String = "",
    val proteins: String = "",
    val fats: String = "",
    val carbs: String = "",
    val calories: String = "",
    val category: String = "Общее",
    val unit: MeasureUnit = MeasureUnit.GRAMS
)
