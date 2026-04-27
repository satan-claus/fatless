package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intervals",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            // Если удаляем воркаут — все интервалы летят следом
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Индекс для быстрого поиска интервалов по ID воркаута
    indices = [Index("workoutId")]
)
data class IntervalEntity(
    @PrimaryKey
    val id: String,
    val workoutId: String,
    val name: String,
    val seconds: Int,
    // PREPARATION, WORK, REST (храним как строку из Enum)
    val type: String,
    // Цель по повторениям (новое поле)
    val reps: Int? = null,
    // Считать шаги в этом интервале
    val trackSteps: Boolean = false,
    // Порядок сортировки в списке
    val sortOrder: Int
)
