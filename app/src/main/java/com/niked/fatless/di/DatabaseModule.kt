package com.niked.fatless.di

import android.content.Context
import androidx.room.Room
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.data.local.AppDatabase
import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.core.utils.Constants
import com.niked.fatless.data.local.DatabasePrepCallback
import com.niked.fatless.data.local.dao.ActivityDao
import com.niked.fatless.data.local.dao.ExerciseDao
import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.local.dao.LocationDao
import com.niked.fatless.data.local.dao.LogDao
import com.niked.fatless.data.local.dao.ShopDao
import com.niked.fatless.data.local.dao.ShoppingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        foodDaoProvider: Provider<FoodDao>,
        exerciseDaoProvider: Provider<ExerciseDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .addCallback(DatabasePrepCallback(context, foodDaoProvider, exerciseDaoProvider))
            .build()
    }


    @Provides
    fun provideActivityDao(db: AppDatabase): ActivityDao {
        return db.activityDao()
    }

    @Provides
    fun provideExerciseDao(db: AppDatabase): ExerciseDao {
        return db.exerciseDao()
    }

    @Provides
    fun provideFoodDao(db: AppDatabase): FoodDao {
        return db.foodDao()
    }

    @Provides
    fun provideLocationDao(db: AppDatabase): LocationDao {
        return db.locationDao()
    }

    @Provides
    fun provideLogDao(db: AppDatabase): LogDao {
        return db.logDao()
    }

    @Provides
    @Singleton
    fun provideAppLogger(logDao: LogDao): AppLogger {
        return AppLogger(logDao)
    }

    @Provides
    fun provideShopDao(db: AppDatabase): ShopDao {
        return db.shopDao()
    }

    @Provides
    fun provideShoppingDao(db: AppDatabase): ShoppingDao {
        return db.shoppingDao()
    }

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao {
        return db.workoutDao()
    }
}