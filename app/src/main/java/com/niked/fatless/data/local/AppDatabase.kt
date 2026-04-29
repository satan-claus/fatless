package com.niked.fatless.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.data.local.entities.WorkoutEntity
import com.niked.fatless.core.utils.Constants.DATABASE_VERSION
import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.local.dao.StepDao
import com.niked.fatless.data.local.entities.FoodCategoryEntity
import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import com.niked.fatless.data.local.entities.StepHistoryEntity

@Database(
    entities = [
        FoodCategoryEntity::class,
        FoodDiaryEntity::class,
        FoodEntity::class,
        IntervalEntity::class,
        StepHistoryEntity::class,
        WorkoutEntity::class,
    ],
    version = DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
}