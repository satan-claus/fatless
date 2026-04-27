package com.niked.fatless.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.niked.fatless.data.local.AppDatabase
import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.core.utils.Constants
import com.niked.fatless.data.local.FoodMocks
import com.niked.fatless.data.local.dao.FoodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        foodDaoProvider: Provider<FoodDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Запускаем заполнение только при ПЕРВОМ создании базы
                CoroutineScope(Dispatchers.IO).launch {
                    FoodMocks.INITIAL_FOOD.forEach {
                        foodDaoProvider.get().insertProduct(it)
                    }
                }
            }
        }).build()
    }

    @Provides
    fun provideFoodDao(db: AppDatabase): FoodDao {
        return db.foodDao()
    }

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao {
        return db.workoutDao()
    }
}