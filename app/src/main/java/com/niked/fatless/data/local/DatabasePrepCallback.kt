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
    private val foodDaoProvider: javax.inject.Provider<FoodDao>
) : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)

        // Запускаем ОДНУ корутину для проверки и вставки
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = foodDaoProvider.get()

                // ПРОВЕРКА: если еды нет — заправляем.
                if (dao.getFoodCount() == 0) {
                    fillDefaultData(dao)
                }
            } catch (e: Exception) {
                Log.e("DB_PREP", "Ошибка в onOpen: ${e.message}")
            }
        }
    }

    // Делаем функцию suspend, чтобы она работала внутри корутины onOpen
    private suspend fun fillDefaultData(dao: FoodDao) {
        try {
            // 1. Читаем JSON
            val jsonString = context.assets.open("initial_data.json")
                .bufferedReader()
                .use { it.readText() }

            // 2. Парсим
            val gson = Gson()
            val data = gson.fromJson(jsonString, InitialData::class.java)

            // 3. Заливаем (в транзакции было бы еще круче, но для старта и так пойдет)
            data.categories.forEach { dao.insertCategory(it) }
            data.foods.forEach { dao.insertProduct(it) }

            // СЮДА ЖЕ ДОБАВИМ НАШИ УПРАЖНЕНИЯ, когда создашь для них DAO
            // dao.insertExerciseTypes(defaultExerciseTypes)

            Log.d("DB_PREP", "Справочники успешно восстановлены")
        } catch (e: Exception) {
            Log.e("DB_PREP", "Ошибка при наполнении БД: ${e.message}")
        }
    }
}
