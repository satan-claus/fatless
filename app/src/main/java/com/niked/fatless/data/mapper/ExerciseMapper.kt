package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.ExerciseTypeEntity
import com.niked.fatless.domain.model.ExerciseType

fun ExerciseTypeEntity.toDomain() = ExerciseType(
    id = id,
    nameResId = nameResId,
    metValue = metValue,
    iconResId = iconResId
)
