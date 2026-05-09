package com.niked.fatless.domain.model

data class DailyActivity(
    val date: String,
    val steps: Int,
    val consumedCalories: Float,
    val burnedCalories: Float,
    val proteins: Float,
    val fats: Float,
    val carbs: Float,
    val weight: Float,
    val hourlySteps: String
)
