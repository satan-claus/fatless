package com.niked.fatless.data.repository

import android.content.Context
import com.niked.fatless.data.local.dao.WorkoutDao
import com.niked.fatless.data.local.entities.WorkoutEntity
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.data.mapper.toEntity
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.domain.model.IntervalType
import com.niked.fatless.domain.model.Workout
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao,
    @ApplicationContext private val context: Context
) : IWorkoutRepository {

    override fun observeAllWorkouts(): Flow<List<Workout>> {
        return dao.observeWorkoutsWithDetails().map { list ->
            list.map { detail ->
                Workout(
                    id = detail.workout.id,
                    title = detail.workout.title,
                    intervals = detail.intervals.map { intervalDetail ->
                        intervalDetail.interval.toDomain(
                            exerciseType = intervalDetail.exerciseType?.toDomain(context)
                        )
                    }
                )
            }
        }
    }

    override suspend fun getWorkoutById(id: String): Workout? {
        // Используем метод из DAO, который возвращает WorkoutWithDetails
        val fullDetail = dao.getWorkoutWithDetailsById(id) ?: return null

        return Workout(
            id = fullDetail.workout.id,
            title = fullDetail.workout.title,
            intervals = fullDetail.intervals.map { intervalDetail ->
                intervalDetail.interval.toDomain(
                    exerciseType = intervalDetail.exerciseType?.toDomain(context)
                )
            }
        )
    }

    override suspend fun saveWorkout(workout: Workout) {
        val workoutEntity = WorkoutEntity(id = workout.id, title = workout.title)

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
                    Interval(name = "Подготовка", seconds = 10, type = IntervalType.PREPARATION),
                    Interval(name = "Работа", seconds = 30, type = IntervalType.WORK),
                    Interval(name = "Отдых", seconds = 15, type = IntervalType.REST)
                )
            )
            saveWorkout(firstWorkout)
        }
    }
}

