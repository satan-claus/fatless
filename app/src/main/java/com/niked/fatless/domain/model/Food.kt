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
    val categoryName: String, // В UI нам всё равно нужно имя
    val categoryId: String,    // А для логики - ID
    val unit: MeasureUnit = MeasureUnit.GRAMS,
    val isCustom: Boolean = false
)
