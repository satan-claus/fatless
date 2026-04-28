package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.mapper.createDiaryEntity
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.data.mapper.toEntity
import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.FoodCategory
import com.niked.fatless.domain.model.MealEntry
import com.niked.fatless.domain.repository.INutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : INutritionRepository {

    override fun searchProducts(query: String): Flow<List<Food>> {
        // 1. Вызываем метод с JOIN-ом
        return foodDao.searchProductsWithCategory(query).map { list ->
            // 2. Теперь маппер toDomain() подходит идеально!
            list.map { it.toDomain() }
        }
    }

    override suspend fun addProductToLibrary(food: Food) {
        foodDao.insertProduct(food.toEntity())
    }

    override suspend fun getProductById(id: String): Food? {
        // Вызываем метод с JOIN, который вернет FoodWithCategory
        return foodDao.getProductWithCategoryById(id)?.toDomain()
    }

    override suspend fun deleteProductFromLibrary(id: String) {
        foodDao.deleteProductById(id)
    }

    override fun getAllCategories(): Flow<List<FoodCategory>> {
        return foodDao.getAllCategories().map { entities ->
            entities.map { entity ->
                FoodCategory(
                    id = entity.categoryId,
                    name = entity.name,
                    icon = entity.icon
                )
            }
        }
    }

    override fun getDiaryForToday(): Flow<List<MealEntry>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endOfDay = calendar.timeInMillis

        return foodDao.getDiaryEntriesForDay(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addMeal(food: Food, weightGrams: Int) {
        val entity = createDiaryEntity(food, weightGrams)
        foodDao.insertDiaryEntry(entity)
    }

    override suspend fun deleteMeal(entryId: Long) {
        foodDao.deleteDiaryEntryById(entryId)
    }
}
