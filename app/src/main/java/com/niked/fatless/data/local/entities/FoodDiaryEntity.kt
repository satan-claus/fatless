package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_diary")
data class FoodDiaryEntity(
    @PrimaryKey(autoGenerate = true)
    val entryId: Long = 0,
    val foodId: String,   // Ссылка на продукт
    val foodName: String, // Дублируем имя на случай удаления продукта из справочника
    val weightGrams: Int, // Сколько съел (например 300г)
    val dateTimestamp: Long, // Дата приема пищи

    // Посчитанные значения на лету для быстрого суммирования
    val calcProteins: Float,
    val calcFats: Float,
    val calcCarbs: Float,
    val calcCalories: Int
)
