package com.niked.fatless.domain.model

import java.util.UUID

data class Interval(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val seconds: Int,
    val type: IntervalType,
    val reps: Int? = null,
    val trackSteps: Boolean = false,
    val exerciseType: ExerciseType? = null
)