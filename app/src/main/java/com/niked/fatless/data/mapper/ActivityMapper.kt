package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.DailyActivityEntity
import com.niked.fatless.domain.model.DailyActivity

/**
 * Маппер для активности дня (История)
 */
fun DailyActivityEntity.toDomain(): DailyActivity {
    return DailyActivity(
        date = this.date,
        steps = this.steps,
        consumedCalories = this.consumedCalories,
        burnedCalories = this.burnedCalories,
        proteins = this.proteins,
        fats = this.fats,
        carbs = this.carbs,
        weight = this.weight,
        hourlySteps = this.hourlySteps
    )
}