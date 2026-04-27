package com.niked.fatless.domain.model

data class Interval(
    val name: String,
    // Жесткое время (таймер идет вниз)
    val seconds: Int,
    val type: IntervalType,
    // Цель по количеству (например, 50)
    val reps: Int? = null,
    val trackSteps: Boolean = false
)