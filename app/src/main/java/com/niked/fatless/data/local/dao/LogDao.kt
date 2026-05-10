package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.niked.fatless.data.local.entity.LogEntity

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM logs ORDER BY id ASC")
    suspend fun getAll(): List<LogEntity>

    @Query("DELETE FROM logs")
    suspend fun clearAll()
}
