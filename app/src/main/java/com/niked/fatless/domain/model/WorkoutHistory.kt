package com.niked.fatless.domain.model

import java.util.UUID

data class WorkoutHistory(
    val id: String = UUID.randomUUID().toString(),
    val workoutTitle: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val totalSeconds: Int,
    val totalSteps: Int,
    val completedIntervals: Int, // Сколько кругов из скольки дожал
    val totalIntervals: Int
)