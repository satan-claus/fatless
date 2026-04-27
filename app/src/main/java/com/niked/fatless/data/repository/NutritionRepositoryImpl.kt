package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.mapper.createDiaryEntity
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.data.mapper.toEntity
import com.niked.fatless.domain.model.Food
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
        return foodDao.searchProducts(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addProductToLibrary(food: Food) {
        foodDao.insertProduct(food.toEntity())
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
