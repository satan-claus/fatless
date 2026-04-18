package com.niked.fatless.domain.model

data class Interval(
    val name: String,
    val seconds: Int,      // Жесткое время (таймер идет вниз)
    val type: IntervalType,
    val reps: Int? = null  // Цель по количеству (например, 50)
)