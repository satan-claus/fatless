package com.niked.fatless.domain.model

data class MealEntry(
    val id: Long = 0,
    val foodName: String,
    val weightGrams: Int,
    val dateTimestamp: Long,
    // Итоговые цифры за порцию
    val totalProteins: Float,
    val totalFats: Float,
    val totalCarbs: Float,
    val totalCalories: Int
)
