package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.FoodCategory
import com.niked.fatless.domain.model.FoodItem
import com.niked.fatless.domain.model.MealEntry
import kotlinx.coroutines.flow.Flow

interface INutritionRepository {
    // Работа со справочником
    fun searchProducts(query: String): Flow<List<FoodItem>>
    suspend fun addProductToLibrary(food: FoodItem)

    suspend fun getProductById(id: String): FoodItem?

    suspend fun deleteProductFromLibrary(id: String)

    fun getAllCategories(): Flow<List<FoodCategory>>

    // Работа с дневником
    fun getDiaryForToday(dateTrigger: String): Flow<List<MealEntry>>
    suspend fun addMeal(food: FoodItem, amount: Int): MealEntry
    suspend fun deleteMeal(entryId: Long)
}
