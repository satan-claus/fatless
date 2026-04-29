package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Каждая запись — это один день
 */
@Entity(tableName = "step_history")
data class StepHistoryEntity(
    @PrimaryKey val date: String, // Формат "yyyy-MM-dd"
    val steps: Int
)