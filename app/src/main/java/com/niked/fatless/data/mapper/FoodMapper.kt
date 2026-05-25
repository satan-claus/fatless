package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import com.niked.fatless.data.local.relation.FoodWithCategory
import com.niked.fatless.domain.model.FoodItem
import com.niked.fatless.domain.model.MeasureUnit
import com.niked.fatless.domain.model.MealEntry

/**
 * 1. Поиск: База (Relation) -> Домен (Food)
 */
fun FoodWithCategory.toDomain() = FoodItem(
    id = food.id,
    name = food.name,
    proteins = food.proteins,
    fats = food.fats,
    carbs = food.carbs,
    calories = food.calories,
    categoryId = food.categoryId,
    categoryName = categoryName,
    unit = MeasureUnit.entries.find { it.name == food.unit } ?: MeasureUnit.GRAMS,
    isCustom = food.isCustom
)

/**
 * 2. Дневник: База (Entity) -> Домен (MealEntry)
 */
fun FoodDiaryEntity.toDomain() = MealEntry(
    id = entryId,
    foodName = foodName,
    quantity = quantity,
    unit = MeasureUnit.entries.find { it.name == unit } ?: MeasureUnit.GRAMS,
    dateTimestamp = dateTimestamp,
    totalProteins = calcProteins,
    totalFats = calcFats,
    totalCarbs = calcCarbs,
    totalCalories = calcCalories
)

/**
 * 3. Сохранение продукта: Домен (Food) -> База (FoodEntity)
 * Тот самый потеряшка!
 */
fun FoodItem.toEntity() = FoodEntity(
    id = id,
    name = name,
    proteins = proteins,
    fats = fats,
    carbs = carbs,
    calories = calories,
    categoryId = categoryId,
    unit = unit.name,
    isCustom = isCustom
)

/**
 * 4. Создание записи для дневника (Food -> FoodDiaryEntity)
 */
fun createDiaryEntity(food: FoodItem, quantity: Int): FoodDiaryEntity {
    val ratio = if (food.unit == MeasureUnit.PIECES) quantity.toFloat() else quantity / 100f

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
