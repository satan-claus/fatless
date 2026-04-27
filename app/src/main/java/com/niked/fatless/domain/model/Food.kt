package com.niked.fatless.domain.model

enum class MeasureUnit(val label: String) {
    GRAMS("г"),
    PIECES("шт"),
    MILLILITERS("мл")
}

data class Food(
    val id: String,
    val name: String,
    val proteins: Float,
    val fats: Float,
    val carbs: Float,
    val calories: Int,
    val category: String = "Общее",
    val unit: MeasureUnit = MeasureUnit.GRAMS,
    val isCustom: Boolean = false
)
