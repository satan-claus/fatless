package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_types")
data class ExerciseTypeEntity(
    @PrimaryKey val id: String,
    // Храним "exercise_walk"
    val nameKey: String,
    // Коэффициент MET
    val metValue: Float,
    // Храним "ic_walk"
    val iconKey: String
)
