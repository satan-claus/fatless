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
    val calories: Int = 0,
    val proteins: Float = 0f,
    val fats: Float = 0f,
    val carbs: Float = 0f
)