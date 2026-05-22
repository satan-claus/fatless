package com.niked.fatless.domain.usecase

import com.niked.fatless.domain.model.MealEntry
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.INutritionRepository
import java.time.LocalDate
import javax.inject.Inject

class DeleteMealUseCase @Inject constructor(
    private val nutritionRepository: INutritionRepository,
    private val activityRepository: IActivityRepository
) {
    suspend operator fun invoke(entry: MealEntry) {
        activityRepository.saveNutrition(
            date = LocalDate.now().toString(),
            consumedCalories = -entry.totalCalories.toFloat(),
            proteins = -entry.totalProteins,
            fats = -entry.totalFats,
            carbs = -entry.totalCarbs
        )

        nutritionRepository.deleteMeal(entry.id)
    }
}
