package com.niked.fatless.data.local.dao

import androidx.room.*
import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.data.local.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // Подписка на список всех тренировок
    @Query("SELECT * FROM workouts")
    fun observeAllWorkouts(): Flow<List<WorkoutEntity>>

    // Получение интервалов для конкретной тренировки
    @Query("SELECT * FROM intervals WHERE workoutId = :workoutId ORDER BY sortOrder")
    suspend fun getIntervalsForWorkout(workoutId: String): List<IntervalEntity>

    // Вставка/Обновление
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntervals(intervals: List<IntervalEntity>)

    // Удаление
    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: String)
}