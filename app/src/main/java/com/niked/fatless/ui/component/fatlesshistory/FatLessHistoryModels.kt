package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.ui.graphics.Color

enum class FatLessHistoryType {
    STEPS, NUTRITION
}

data class HistoryBarModel(
    val label: String,        // "Пн", "Вт"...
    val value: Float,         // Текущие шаги
    val goal: Float,          // Цель (для линии)
    val isToday: Boolean,     // Только если это реально СЕГОДНЯ
    val isFuture: Boolean,    // Если день еще не наступил
    val barColor: Color,      // AppOrange или AppPrimary
    val showStar: Boolean     // Звезда при выполнении цели
)

data class NutritionBarModel(
    val dayLabel: String,
    val proteins: Float,
    val fats: Float,
    val carbs: Float,
    val totalCalories: Float,
    val isToday: Boolean,
    val isFuture: Boolean
)
