package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey
    // UUID
    val id: String,
    val title: String
)