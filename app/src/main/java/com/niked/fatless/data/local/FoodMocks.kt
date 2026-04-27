package com.niked.fatless.data.local

import com.niked.fatless.data.local.entities.FoodEntity

object FoodMocks {
    val INITIAL_FOOD = listOf(
        // Имя, Белки, Жиры, Угли, Калории (на 100г)
        FoodEntity("f1", "Куриная грудка (отварная)", 29.8f, 1.8f, 0.5f, 150),
        FoodEntity("f2", "Рис отварной", 2.2f, 0.5f, 24.9f, 116),
        FoodEntity("f3", "Яйцо куриное (1 шт)", 12.7f, 11.5f, 0.7f, 157),
        FoodEntity("f4", "Огурец свежий", 0.8f, 0.1f, 2.8f, 15),
        FoodEntity("f5", "Стейк из говядины", 25.0f, 15.0f, 0.0f, 250),
        FoodEntity("f6", "Пиво светлое", 0.5f, 0.0f, 3.5f, 43),
        FoodEntity("f7", "Творог 5%", 16.0f, 5.0f, 3.0f, 121),
        FoodEntity("f8", "Гречка отварная", 4.2f, 1.1f, 21.3f, 110),
        FoodEntity("f9", "Масло оливковое", 0.0f, 99.8f, 0.0f, 898),
        FoodEntity("f10", "Банан", 1.5f, 0.2f, 21.8f, 95)
    )
}
