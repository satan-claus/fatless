package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.repository.INutritionRepository
import com.niked.fatless.domain.model.FoodItem
import com.niked.fatless.domain.model.MealEntry
import com.niked.fatless.domain.usecase.AddMealUseCase
import com.niked.fatless.domain.usecase.DeleteMealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: INutritionRepository,
    private val addMealUseCase: AddMealUseCase,
    private val deleteMealUseCase: DeleteMealUseCase
) : ViewModel() {

    private val currentDate = flow {
        while(true) {
            emit(LocalDate.now().toString())
            delay(60000)
        }
    }.distinctUntilChanged()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList<FoodItem>())
            else nutritionRepository.searchProducts(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val diaryEntries: StateFlow<List<MealEntry>> = currentDate
        .flatMapLatest { date ->
            // Передаем дату как "пинок", чтобы репозиторий пересчитал Calendar
            nutritionRepository.getDiaryForToday(date)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<NutritionUiState> = diaryEntries.map { entries ->
        NutritionUiState(
            totalProteins = entries.sumOf { it.totalProteins.toDouble() }.toFloat(),
            totalFats = entries.sumOf { it.totalFats.toDouble() }.toFloat(),
            totalCarbs = entries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
            totalCalories = entries.sumOf { it.totalCalories.toDouble() }.toFloat()
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, NutritionUiState())

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Добавление еды
    fun addMeal(food: FoodItem, amount: Int) {
        viewModelScope.launch {
            addMealUseCase(food, amount)
            onQueryChange("")
        }
    }

    // Удаление через корзину
    fun deleteMeal(entry: MealEntry) {
        viewModelScope.launch {
            deleteMealUseCase(entry)
        }
    }

    fun deleteProductFromLibrary(id: String) {
        viewModelScope.launch {
            nutritionRepository.deleteProductFromLibrary(id)
        }
    }
}

data class NutritionUiState(
    val totalProteins: Float = 0f,
    val totalFats: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalCalories: Float = 0f
)
