package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
data class WorkoutHistoryEntity(
    @PrimaryKey val id: String,
    val workoutTitle: String,
    val dateTimestamp: Long,
    val totalSeconds: Int,
    val totalSteps: Int,
    val completedIntervals: Int,
    val totalIntervals: Int
)