package com.niked.fatless.domain.usecase

import com.niked.fatless.domain.model.Food
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.INutritionRepository
import java.time.LocalDate
import javax.inject.Inject

class AddMealUseCase @Inject constructor(
    private val nutritionRepository: INutritionRepository,
    private val activityRepository: IActivityRepository
) {
    suspend operator fun invoke(food: Food, amount: Int) {
        // 1. Добавляем в детальный дневник
        // Репозиторий сам вернет нам созданную запись в доменном виде (DiaryEntry)
        val entry = nutritionRepository.addMeal(food, amount)

        // 2. Обновляем общую статистику дня
        activityRepository.saveNutrition(
            date = LocalDate.now().toString(),
            cal = entry.totalCalories,
            p = entry.totalProteins,
            f = entry.totalFats,
            c = entry.totalCarbs
        )
    }
}
