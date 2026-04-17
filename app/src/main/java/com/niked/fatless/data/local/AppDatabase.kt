package com.niked.fatless.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niked.fatless.utils.Constants.DATABASE_VERSION

@Database(
    entities = [/* тут будут наши Entity */],
    version = DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    // тут будут функции для Dao
}