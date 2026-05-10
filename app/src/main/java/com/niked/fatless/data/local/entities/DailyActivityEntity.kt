package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Каждая запись — это один день
 */
@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    // "yyyy-MM-dd"
    @PrimaryKey val date: String,
    val steps: Int = 0,
    // Съеденное
    val consumedCalories: Float = 0f,
    // Сожженное (фиксируем по текущему весу)
    val burnedCalories: Float = 0f,
    val proteins: Float = 0f,
    val fats: Float = 0f,
    val carbs: Float = 0f,
    val weight: Float = 0f,
    // 8 интервалов по 3 часа
    val hourlySteps: String = "0,0,0,0,0,0,0,0",
    val sessionId: Long = 0
)