package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.MealEntry
import kotlinx.coroutines.flow.Flow

interface INutritionRepository {
    // Работа со справочником
    fun searchProducts(query: String): Flow<List<Food>>
    suspend fun addProductToLibrary(food: Food)

    // Работа с дневником
    fun getDiaryForToday(): Flow<List<MealEntry>>
    suspend fun addMeal(food: Food, weightGrams: Int)
    suspend fun deleteMeal(entryId: Long)
}
