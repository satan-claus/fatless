package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.niked.fatless.data.local.entities.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(entity: DailyActivityEntity)

    @Query("SELECT * FROM daily_activity ORDER BY date DESC LIMIT 30")
    fun getActivityHistory(): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun getActivityByDate(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getActivityForMonth(monthPrefix: String): Flow<List<DailyActivityEntity>>

}
