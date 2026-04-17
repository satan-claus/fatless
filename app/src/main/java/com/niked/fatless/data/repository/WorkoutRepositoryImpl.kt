package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.data.local.entities.IntervalEntity
import com.niked.fatless.data.local.entities.WorkoutEntity
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.IntervalType
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun observeAllWorkouts(): Flow<List<Workout>> {
        return dao.observeAllWorkouts().map { entities ->
            entities.map { Workout(id = it.id, title = it.title, intervals = emptyList()) }
        }
    }

    override suspend fun getWorkoutById(id: String): Workout? {
        // Тут можно будет оптимизировать через Room @Relation, но пока по-простому
        val intervals = dao.getIntervalsForWorkout(id).map {
            Interval(name = it.name, seconds = it.seconds, type = IntervalType.valueOf(it.type))
        }
        // Временно возвращаем так, пока не добавим метод получения WorkoutEntity по ID в Dao
        return null
    }

    override suspend fun saveWorkout(workout: Workout) {
        val workoutEntity = WorkoutEntity(id = workout.id, title = workout.title)
        val intervalEntities = workout.intervals.mapIndexed { index, interval ->
            IntervalEntity(
                id = UUID.randomUUID().toString(),
                workoutId = workout.id,
                name = interval.name,
                seconds = interval.seconds,
                type = interval.type.name,
                sortOrder = index
            )
        }
        dao.insertWorkout(workoutEntity)
        dao.insertIntervals(intervalEntities)
    }

    override suspend fun deleteWorkout(id: String) = dao.deleteWorkoutById(id)

    override suspend fun initializeDefaultData() {
        // Проверка на пустоту и вставка дефолтов (код возьмем из вчерашних наработок)
    }
}