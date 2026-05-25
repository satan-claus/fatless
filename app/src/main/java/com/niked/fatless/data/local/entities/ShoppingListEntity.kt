package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Ссылка на ID из справочника FoodEntity
    val foodId: Int,
    // "Пиво", "Хлеб"
    val name: String,
    // "Напитки", "Продукты", "Аптека" — это наш ключ для поиска магазина
    val category: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)