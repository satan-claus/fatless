package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.dao.FoodWithCategory
import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.model.MealEntry
import com.niked.fatless.domain.model.MeasureUnit

// --- Маппинг справочника ---

/**
 * 1. Основной маппер для ПОИСКА.
 * Используем FoodWithCategory, потому что Room вытягивает данные сразу из двух таблиц (JOIN).
 */
fun FoodWithCategory.toDomain() = Food(
    id = food.id,
    name = food.name,
    proteins = food.proteins,
    fats = food.fats,
    carbs = food.carbs,
    calories = food.calories,
    categoryId = food.categoryId,
    categoryName = categoryName, // Имя подтянулось из таблицы категорий
    unit = try { MeasureUnit.valueOf(food.unit) } catch (e: Exception) { MeasureUnit.GRAMS },
    isCustom = food.isCustom
)

/**
 * 2. Маппер для СОХРАНЕНИЯ (из экрана создания в БД).
 * Здесь мы превращаем чистую доменную модель обратно в плоскую Entity.
 */
fun Food.toEntity() = FoodEntity(
    id = id,
    name = name,
    proteins = proteins,
    fats = fats,
    carbs = carbs,
    calories = calories,
    categoryId = categoryId, // Сохраняем только ID категории
    unit = unit.name,
    isCustom = isCustom
)

// --- Маппинг дневника ---

/**
 * 3. Из базы в домен (для отображения списка съеденного).
 */
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

/**
 * 4. Создание записи для дневника (когда Джон нажал "Добавить").
 */
fun createDiaryEntity(food: Food, quantity: Int): FoodDiaryEntity {
    // Если "шт" — считаем как единицы, если "г/мл" — делим на 100
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
