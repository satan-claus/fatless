package com.niked.fatless.domain.model

data class Shop(
    val id: Int,
    val name: String,
    val categories: List<String>,
    val radius: Float,
    val latitude: Double,
    val longitude: Double
)