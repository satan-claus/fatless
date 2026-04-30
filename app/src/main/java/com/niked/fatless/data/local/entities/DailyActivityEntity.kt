package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Каждая запись — это один день
 */
@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val steps: Int = 0,
    val calories: Float = 0f, // Съеденное
    val burnedCalories: Float = 0f, // Сожженное (фиксируем по текущему весу)
    val proteins: Float = 0f,
    val fats: Float = 0f,
    val carbs: Float = 0f
)