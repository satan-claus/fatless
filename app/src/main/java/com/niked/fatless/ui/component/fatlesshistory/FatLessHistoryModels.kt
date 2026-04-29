package com.niked.fatless.ui.component.fatlesshistory

import androidx.compose.ui.graphics.Color

// Типы истории для переключателя
enum class FatLessHistoryType {
    STEPS, NUTRITION
}

// Модель для обычного столбика (Шаги)
data class HistoryBarModel(
    val label: String,        // "Пн", "Вт" ...
    val value: Float,         // Количество шагов
    val goal: Float,          // Цель
    val isToday: Boolean,     // Акцент на сегодня
    val barColor: Color,      // Цвет столбика
    val showStar: Boolean     // Золотая звезда успеха
)

// Модель для слоеного столбика (Питание в стиле MIUI Storage)
data class NutritionBarModel(
    val dayLabel: String,     // "Пн", "Вт" ...
    val proteins: Float,      // Граммы белков
    val fats: Float,          // Граммы жиров
    val carbs: Float,         // Граммы углеводов
    val totalCalories: Int,   // Сумма калорий
    val isToday: Boolean      // Акцент на сегодня
)
