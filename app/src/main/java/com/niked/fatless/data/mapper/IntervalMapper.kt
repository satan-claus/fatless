package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.domain.model.ExerciseType
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.IntervalType

// Из базы в бизнес-логику (Entity -> Domain)
fun IntervalEntity.toDomain(exerciseType: ExerciseType?): Interval {
    return Interval(
        id = this.id,
        name = this.name,
        seconds = this.seconds,
        type = IntervalType.valueOf(this.type),
        reps = this.reps,
        trackSteps = this.trackSteps,
        exerciseType = exerciseType
    )
}

// Из бизнес-логики в базу (Domain -> Entity)
fun Interval.toEntity(workoutId: String, order: Int): IntervalEntity {
    return IntervalEntity(
        id = this.id,
        workoutId = workoutId,
        name = this.name,
        seconds = this.seconds,
        type = this.type.name,
        reps = this.reps,
        trackSteps = this.trackSteps,
        sortOrder = order,
        exerciseTypeId = this.exerciseType?.id
    )
}
