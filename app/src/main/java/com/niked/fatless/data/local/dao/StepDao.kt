package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.niked.fatless.data.local.entities.StepHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(entity: StepHistoryEntity)

    @Query("SELECT * FROM step_history ORDER BY date DESC LIMIT 7")
    fun getLastWeekHistory(): Flow<List<StepHistoryEntity>>
}
