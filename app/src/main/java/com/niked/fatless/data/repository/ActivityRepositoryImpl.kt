package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.ActivityDao
import com.niked.fatless.data.local.entities.DailyActivityEntity
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.domain.repository.IActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao
) : IActivityRepository {

    override fun getActivityHistory(): Flow<List<DailyActivity>> {
        return activityDao.getActivityHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveSteps(date: String, steps: Int, currentWeight: Float) {
        val current = activityDao.getActivityByDate(date) ?: DailyActivityEntity(date)
        // Считаем расход здесь и сейчас
        val burned = steps.toFloat() * currentWeight * 0.0005f
        activityDao.insertActivity(
            current.copy(
                steps = steps,
                burnedCalories = burned
            )
        )
    }

    override suspend fun saveNutrition(date: String, cal: Int, p: Float, f: Float, c: Float) {
        val current = activityDao.getActivityByDate(date) ?: DailyActivityEntity(date)
        activityDao.insertActivity(
            current.copy(
                consumedCalories = (current.consumedCalories + cal).coerceAtLeast(0f),
                proteins = (current.proteins + p).coerceAtLeast(0f),
                fats = (current.fats + f).coerceAtLeast(0f),
                carbs = (current.carbs + c).coerceAtLeast(0f)
            )
        )
    }

    override fun getActivityForMonth(month: String): Flow<List<DailyActivity>> {
        return activityDao.getActivityForMonth(month).map { list ->
            list.map { it.toDomain() }
        }
    }
}
