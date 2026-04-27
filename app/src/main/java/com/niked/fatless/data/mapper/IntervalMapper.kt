package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.IntervalType
import java.util.UUID

// Из базы в бизнес-логику (Entity -> Domain)
fun IntervalEntity.toDomain(): Interval {
    return Interval(
        name = name,
        seconds = seconds,
        type = IntervalType.valueOf(type),
        reps = reps,
        trackSteps = trackSteps
    )
}

// Из бизнес-логики в базу (Domain -> Entity)
fun Interval.toEntity(workoutId: String, index: Int): IntervalEntity {
    return IntervalEntity(
        id = UUID.randomUUID().toString(),
        workoutId = workoutId,
        name = name,
        seconds = seconds,
        type = type.name,
        reps = reps,
        trackSteps = trackSteps,
        sortOrder = index
    )
}
