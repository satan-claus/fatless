package com.niked.fatless.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.niked.fatless.data.local.entities.ExerciseTypeEntity
import com.niked.fatless.data.local.entities.IntervalEntity

data class IntervalWithDetails(
    @Embedded val interval: IntervalEntity,
    @Relation(
        parentColumn = "exerciseTypeId",
        entityColumn = "id"
    )
    val exerciseType: ExerciseTypeEntity?
)