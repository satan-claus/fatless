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
        // 1. Откатываем калории в общей статистике (используем ПРАВИЛЬНЫЕ имена полей)
        activityRepository.saveNutrition(
            date = LocalDate.now().toString(),
            cal = -entry.totalCalories,
            p = -entry.totalProteins,
            f = -entry.totalFats,
            c = -entry.totalCarbs
        )

        // 2. Удаляем запись из дневника по ID
        nutritionRepository.deleteMeal(entry.id)
    }
}
