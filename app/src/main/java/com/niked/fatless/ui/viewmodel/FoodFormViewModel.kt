package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.MeasureUnit
import com.niked.fatless.domain.repository.INutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FoodFormViewModel @Inject constructor(
    private val repository: INutritionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val foodId: String? = savedStateHandle["foodId"]
    private val initName: String = savedStateHandle["initName"] ?: ""

    private val _uiState = MutableStateFlow(FoodFormUiState(name = initName))
    val uiState = _uiState.asStateFlow()

    // 1. Стрим категорий из базы для чипсов выбора
    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Если прилетел ID — подгружаем данные для редактирования
        foodId?.let { id ->
            viewModelScope.launch {
                repository.getProductById(id)?.let { food ->
                    _uiState.update { it.copy(
                        name = food.name,
                        proteins = food.proteins.toString(),
                        fats = food.fats.toString(),
                        carbs = food.carbs.toString(),
                        calories = food.calories.toString(),
                        categoryId = food.categoryId,
                        unit = food.unit
                    ) }
                }
            }
        }
    }

    // --- МЕТОДЫ ОБНОВЛЕНИЯ ПОЛЕЙ ---

    fun updateName(v: String) = _uiState.update { it.copy(name = v) }

    fun updateProteins(v: String) = _uiState.update { it.copy(proteins = v) }

    fun updateFats(v: String) = _uiState.update { it.copy(fats = v) }

    fun updateCarbs(v: String) = _uiState.update { it.copy(carbs = v) }

    fun updateCalories(v: String) = _uiState.update { it.copy(calories = v) }

    fun updateUnit(v: MeasureUnit) = _uiState.update { it.copy(unit = v) }

    fun updateCategory(id: String) = _uiState.update { it.copy(categoryId = id) }

    // --- СОХРАНЕНИЕ ---

    fun saveProduct(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) return

        val food = Food(
            id = foodId ?: UUID.randomUUID().toString(),
            name = state.name,
            proteins = state.proteins.replace(",", ".").toFloatOrNull() ?: 0f,
            fats = state.fats.replace(",", ".").toFloatOrNull() ?: 0f,
            carbs = state.carbs.replace(",", ".").toFloatOrNull() ?: 0f,
            calories = state.calories.toIntOrNull() ?: 0,
            categoryId = state.categoryId,
            // Имя подтянется через JOIN при поиске
            categoryName = "",
            unit = state.unit,
            isCustom = true
        )

        viewModelScope.launch {
            repository.addProductToLibrary(food)
            onSuccess()
        }
    }
}

data class FoodFormUiState(
    val name: String = "",
    val proteins: String = "",
    val fats: String = "",
    val carbs: String = "",
    val calories: String = "",
    val categoryId: String = "cat_meat",
    val unit: MeasureUnit = MeasureUnit.GRAMS
)

