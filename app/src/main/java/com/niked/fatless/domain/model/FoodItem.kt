package com.niked.fatless.domain.model

import android.content.Context
import com.niked.fatless.R

enum class MeasureUnit(val label: String) {
    GRAMS("г"),
    PIECES("шт"),
    MILLILITERS("мл")
}

data class FoodItem(
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

fun FoodItem.getReadableUnit(context: Context): String {
    val resId = when (this.unit) {
        MeasureUnit.GRAMS -> R.string.unit_gr
        MeasureUnit.MILLILITERS -> R.string.unit_ml
        MeasureUnit.PIECES -> R.string.unit_pc
        else -> R.string.unit_gr
    }
    return context.getString(resId)
}