package com.niked.fatless.domain.model

data class Interval(
    val name: String,
    val seconds: Int,
    val type: IntervalType = IntervalType.WORK
)