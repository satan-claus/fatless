package com.niked.fatless.data.local.dao

import androidx.room.*
import com.niked.fatless.data.local.entities.ExerciseTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_types")
    fun getAllExerciseTypes(): Flow<List<ExerciseTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<ExerciseTypeEntity>)
}
