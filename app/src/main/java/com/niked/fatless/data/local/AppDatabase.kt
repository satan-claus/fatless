package com.niked.fatless.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.data.local.entities.WorkoutEntity
import com.niked.fatless.utils.Constants.DATABASE_VERSION

@Database(
    entities = [
        WorkoutEntity::class,
        IntervalEntity::class
    ],
    version = DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}