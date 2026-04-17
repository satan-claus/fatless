package com.niked.fatless.domain.model

import java.util.UUID

data class Workout(
    // Сразу вешаем UUID
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val intervals: List<Interval>
)