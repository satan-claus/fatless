package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.FoodCategory
import com.niked.fatless.domain.model.MealEntry
import kotlinx.coroutines.flow.Flow

interface INutritionRepository {
    // Работа со справочником
    fun searchProducts(query: String): Flow<List<Food>>
    suspend fun addProductToLibrary(food: Food)

    suspend fun getProductById(id: String): Food?

    suspend fun deleteProductFromLibrary(id: String)

    fun getAllCategories(): Flow<List<FoodCategory>>

    // Работа с дневником
    fun getDiaryForToday(dateTrigger: String): Flow<List<MealEntry>>
    suspend fun addMeal(food: Food, amount: Int): MealEntry
    suspend fun deleteMeal(entryId: Long)
}
