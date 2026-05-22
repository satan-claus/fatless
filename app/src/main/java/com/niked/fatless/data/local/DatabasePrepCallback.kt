package com.niked.fatless.data.local

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.niked.fatless.data.local.dao.ExerciseDao
import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.local.entities.ExerciseTypeEntity
import com.niked.fatless.data.local.entities.FoodCategoryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

data class InitialData(
    val categories: List<FoodCategoryEntity>,
    val foods: List<FoodEntity>,
    val exerciseTypes: List<ExerciseTypeEntity>
)

class DatabasePrepCallback(
    private val context: Context,
    private val foodDaoProvider: Provider<FoodDao>,
    private val exerciseDaoProvider: Provider<ExerciseDao>
) : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)

        // Запускаем ОДНУ корутину для проверки и вставки
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val foodDao = foodDaoProvider.get()
                val exerciseDao = exerciseDaoProvider.get()

                // Проверяем наполнение обеих таблиц
                val isFoodEmpty = foodDao.getFoodCount() == 0
                val isExerciseEmpty = exerciseDao.getExerciseCount() == 0

                // ПРОВЕРКА: если еды нет — заправляем.
                if (isFoodEmpty || isExerciseEmpty) {
                    fillDefaultData(foodDao, exerciseDao, isFoodEmpty, isExerciseEmpty)
                }
            } catch (e: Exception) {
                Log.e("DB_PREP", "Ошибка в onOpen: ${e.message}")
            }
        }
    }

    // Делаем функцию suspend, чтобы она работала внутри корутины onOpen
    private suspend fun fillDefaultData(
        foodDao: FoodDao,
        exerciseDao: ExerciseDao,
        fillFood: Boolean,
        fillExercise: Boolean
    ) {
        try {
            // 1. Читаем JSON
            val jsonString = context.assets.open("initial_data.json")
                .bufferedReader()
                .use { it.readText() }

            // 2. Парсим
            val gson = Gson()
            val data = gson.fromJson(jsonString, InitialData::class.java)

            // Заливаем еду, только если её нет
            if (fillFood) {
                data.categories.forEach { foodDao.insertCategory(it) }
                data.foods.forEach { foodDao.insertProduct(it) }
            }

            // Заливаем упражнения, только если их нет
            if (fillExercise) {
                exerciseDao.insertAll(data.exerciseTypes)
            }

            Log.d("DB_PREP", "База дозаправлена: еда=$fillFood, упражнения=$fillExercise")
        } catch (e: Exception) {
            Log.e("DB_PREP", "Ошибка при наполнении БД: ${e.message}")
        }
    }
}
