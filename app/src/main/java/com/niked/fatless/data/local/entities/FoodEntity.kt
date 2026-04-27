package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val proteins: Float, // на 100г
    val fats: Float,     // на 100г
    val carbs: Float,    // на 100г
    val calories: Int,   // на 100г
    val category: String = "Общее",
    val unit: String = "GRAMS", // Храним как строку
    val isCustom: Boolean = false // Создано юзером или системное
)
