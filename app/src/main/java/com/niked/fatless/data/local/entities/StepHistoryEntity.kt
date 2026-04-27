package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_history")
data class StepHistoryEntity(
    @PrimaryKey val date: String, // Формат "2024-05-20"
    val steps: Int
)