package com.niked.fatless.domain.model

data class Food(
    val id: String,
    val name: String,
    val proteins: Float,
    val fats: Float,
    val carbs: Float,
    val calories: Int,
    val isCustom: Boolean = false
)
