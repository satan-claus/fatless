package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_types")
data class ExerciseTypeEntity(
    @PrimaryKey val id: String,
    // Ссылка на R.string
    val nameResId: Int,
    // Коэффициент MET
    val metValue: Float,
    // Ссылка на R.drawable
    val iconResId: Int
)
