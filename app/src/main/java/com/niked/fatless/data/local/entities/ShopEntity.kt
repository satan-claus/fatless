package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // "Красное и Белое", "Магнит"
    val name: String,
    // "Пиво", "Продукты" — должна совпадать с категорией в списке
    val category: String,
    val latitude: Double,
    val longitude: Double,
    // Радиус срабатывания в метрах
    val radius: Float = 500f
)