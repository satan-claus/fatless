package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_categories")
data class FoodCategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val name: String,
    val icon: String? = null
)