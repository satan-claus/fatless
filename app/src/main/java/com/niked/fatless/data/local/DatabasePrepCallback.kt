package com.niked.fatless.data.local

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.local.entities.FoodCategoryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class InitialData(
    val categories: List<FoodCategoryEntity>,
    val foods: List<FoodEntity>
)

class DatabasePrepCallback(
    private val context: Context,
    // Используем Provider для Hilt
    private val foodDaoProvider: javax.inject.Provider<FoodDao>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Запускаем корутину, так как запись в БД - долгая операция
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Читаем файл из assets
                val jsonString = context.assets.open("initial_data.json")
                    .bufferedReader()
                    .use { it.readText() }

                // 2. Парсим JSON через GSON
                val gson = Gson()
                val data = gson.fromJson(jsonString, InitialData::class.java)

                val dao = foodDaoProvider.get()

                // 3. Сначала заливаем категории (важно из-за связей!)
                data.categories.forEach { dao.insertCategory(it) }

                // 4. Затем заливаем продукты
                data.foods.forEach { dao.insertProduct(it) }

                Log.d("DB_PREP", "База успешно инициализирована данными")
            } catch (e: Exception) {
                Log.e("DB_PREP", "Ошибка при загрузке JSON: ${e.message}")
            }
        }
    }
}