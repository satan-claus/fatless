package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.MealEntry
import com.niked.fatless.domain.model.MeasureUnit

// --- Маппинг справочника ---

// Из базы в домен
fun FoodEntity.toDomain() = Food(
    id = id,
    name = name,
    proteins = proteins,
    fats = fats,
    carbs = carbs,
    calories = calories,
    category = category,
    unit = try { MeasureUnit.valueOf(unit) } catch (e: Exception) { MeasureUnit.GRAMS },
    isCustom = isCustom
)

// Из домена в базу
fun Food.toEntity() = FoodEntity(
    id = id,
    name = name,
    proteins = proteins,
    fats = fats,
    carbs = carbs,
    calories = calories,
    category = category,
    unit = unit.name,
    isCustom = isCustom
)

// --- Маппинг дневника ---

fun FoodDiaryEntity.toDomain() = MealEntry(
    id = entryId,
    foodName = foodName,
    weightGrams = weightGrams,
    dateTimestamp = dateTimestamp,
    totalProteins = calcProteins,
    totalFats = calcFats,
    totalCarbs = calcCarbs,
    totalCalories = calcCalories
)

/**
 * Создаем запись для БД из выбранного продукта и его веса
 */
fun createDiaryEntity(food: Food, weight: Int): FoodDiaryEntity {
    val ratio = weight / 100f
    return FoodDiaryEntity(
        foodId = food.id,
        foodName = food.name,
        weightGrams = weight,
        dateTimestamp = System.currentTimeMillis(),
        calcProteins = food.proteins * ratio,
        calcFats = food.fats * ratio,
        calcCarbs = food.carbs * ratio,
        calcCalories = (food.calories * ratio).toInt()
    )
}
