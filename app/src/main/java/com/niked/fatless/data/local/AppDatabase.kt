package com.niked.fatless.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.data.local.entities.WorkoutEntity
import com.niked.fatless.core.utils.Constants.DATABASE_VERSION
import com.niked.fatless.data.local.dao.ActivityDao
import com.niked.fatless.data.local.dao.ExerciseDao
import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.local.dao.LogDao
import com.niked.fatless.data.local.entities.DailyActivityEntity
import com.niked.fatless.data.local.entities.ExerciseTypeEntity
import com.niked.fatless.data.local.entities.FoodCategoryEntity
import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import com.niked.fatless.data.local.entity.LogEntity

@Database(
    entities = [
        DailyActivityEntity::class,
        ExerciseTypeEntity::class,
        FoodCategoryEntity::class,
        FoodDiaryEntity::class,
        FoodEntity::class,
        IntervalEntity::class,
        LogEntity::class,
        WorkoutEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun foodDao(): FoodDao
    abstract fun logDao(): LogDao
    abstract fun workoutDao(): WorkoutDao
}