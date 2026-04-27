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

// Из базы в домен
fun FoodDiaryEntity.toDomain() = MealEntry(
    id = entryId,
    foodName = foodName,
    quantity = quantity,
    unit = try { MeasureUnit.valueOf(unit) } catch (e: Exception) { MeasureUnit.GRAMS },
    dateTimestamp = dateTimestamp,
    totalProteins = calcProteins,
    totalFats = calcFats,
    totalCarbs = calcCarbs,
    totalCalories = calcCalories
)

// Создание записи для дневника
fun createDiaryEntity(food: Food, quantity: Int): FoodDiaryEntity {
    val ratio = if (food.unit == MeasureUnit.PIECES) {
        quantity.toFloat()
    } else {
        quantity / 100f
    }

    return FoodDiaryEntity(
        foodId = food.id,
        foodName = food.name,
        quantity = quantity,
        unit = food.unit.name,
        dateTimestamp = System.currentTimeMillis(),
        calcProteins = food.proteins * ratio,
        calcFats = food.fats * ratio,
        calcCarbs = food.carbs * ratio,
        calcCalories = (food.calories * ratio).toInt()
    )
}
