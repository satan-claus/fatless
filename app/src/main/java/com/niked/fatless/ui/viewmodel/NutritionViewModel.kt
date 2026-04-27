package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.repository.INutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repository: INutritionRepository
) : ViewModel() {

    // 1. Поиск (Query + Результаты)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchProducts(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 2. Дневник (Список съеденного за сегодня)
    val diaryEntries = repository.getDiaryForToday()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 3. Итоговое состояние для кружка (КБЖУ за день)
    // Мы склеиваем список записей в один объект статистики
    val uiState: StateFlow<NutritionUiState> = diaryEntries.map { entries ->
        NutritionUiState(
            totalProteins = entries.sumOf { it.totalProteins.toDouble() }.toFloat(),
            totalFats = entries.sumOf { it.totalFats.toDouble() }.toFloat(),
            totalCarbs = entries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
            totalCalories = entries.sumOf { it.totalCalories }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, NutritionUiState())

    // --- ЛОГИКА ---

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addMeal(food: Food, weight: Int) {
        viewModelScope.launch {
            repository.addMeal(food, weight)
            onQueryChange("") // Сбрасываем поиск после добавления
        }
    }

    fun deleteMeal(entryId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(entryId)
        }
    }
}

data class NutritionUiState(
    val totalProteins: Float = 0f,
    val totalFats: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalCalories: Int = 0
)
