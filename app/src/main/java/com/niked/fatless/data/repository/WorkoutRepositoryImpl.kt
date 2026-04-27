package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.data.local.entities.WorkoutEntity
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.data.mapper.toEntity
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.IntervalType
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.IWorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : IWorkoutRepository {

    override fun observeAllWorkouts(): Flow<List<Workout>> {
        return dao.observeAllWorkouts().map { entities ->
            entities.map { entity ->
                // Достаем интервалы для каждой тренировки, чтобы посчитать их
                val intervals = dao.getIntervalsForWorkout(entity.id).map {
                    Interval(it.name, it.seconds, IntervalType.valueOf(it.type))
                }
                Workout(id = entity.id, title = entity.title, intervals = intervals)
            }
        }
    }

    override suspend fun getWorkoutById(id: String): Workout? {
        val entity = dao.getWorkoutById(id) ?: return null

        val intervals = dao.getIntervalsForWorkout(id).map { it.toDomain() }

        return Workout(id = entity.id, title = entity.title, intervals = intervals)
    }

    override suspend fun saveWorkout(workout: Workout) {
        val workoutEntity = WorkoutEntity(id = workout.id, title = workout.title)

        // Генерируем сущности интервалов с привязкой к ID тренировки
        val intervalEntities = workout.intervals.mapIndexed { index, interval ->
            interval.toEntity(workout.id, index)
        }

        dao.insertWorkout(workoutEntity)
        dao.insertIntervals(intervalEntities)
    }

    override suspend fun deleteWorkout(id: String) {
        dao.deleteWorkoutById(id)
    }

    override suspend fun initializeDefaultData() {
        val existing = dao.getWorkoutsOnce()
        if (existing.isEmpty()) {
            val firstWorkout = Workout(
                title = "Базовая разминка",
                intervals = listOf(
                    Interval("Подготовка", 5, IntervalType.PREPARATION),
                    Interval("Работа", 5, IntervalType.WORK),
                    Interval("Отдых", 5, IntervalType.REST)
                )
            )
            saveWorkout(firstWorkout)
        }
    }
}
