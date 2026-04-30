package com.niked.fatless.data.local.relation

import androidx.room.Embedded
import com.niked.fatless.data.local.entities.FoodEntity

// Вспомогательный класс для результата запроса
data class FoodWithCategory(
    @Embedded val food: FoodEntity,
    val categoryName: String
)