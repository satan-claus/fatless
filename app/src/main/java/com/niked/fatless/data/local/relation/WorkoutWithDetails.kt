package com.niked.fatless.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.data.local.entities.WorkoutEntity

data class WorkoutWithDetails(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = IntervalEntity::class,
        parentColumn = "id",      // Это ID из WorkoutEntity
        entityColumn = "workoutId" // Это колонка в IntervalEntity
    )
    val intervals: List<IntervalWithDetails>
)