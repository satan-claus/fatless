package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.ActivityDao
import com.niked.fatless.data.local.dao.LocationDao
import com.niked.fatless.data.local.entities.DailyActivityEntity
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.data.mapper.toEntity
import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.domain.model.UserLocation
import com.niked.fatless.domain.repository.IActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val locationDao: LocationDao
) : IActivityRepository {

    override fun getActivityHistory(): Flow<List<DailyActivity>> {
        return activityDao.getActivityHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActivityForMonth(month: String): Flow<List<DailyActivity>> {
        return activityDao.getActivityForMonth(month).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveSteps(
        date: String,
        steps: Int,
        burnedCalories: Float,
        currentWeight: Float,
        hourlySteps: String
    ) {
        val entity = activityDao.getActivityByDateOnce(date) ?: DailyActivityEntity(date = date)
        activityDao.insertActivity(
            entity.copy(
                steps = steps,
                burnedCalories = burnedCalories,
                weight = currentWeight,
                hourlySteps = hourlySteps
            )
        )
    }

    override suspend fun saveNutrition(
        date: String,
        consumedCalories: Float,
        proteins: Float,
        fats: Float,
        carbs: Float
    ) {
        // 1. Достаем то, что уже есть в базе за этот день
        val entity = activityDao.getActivityByDateOnce(date) ?: DailyActivityEntity(date = date)

        // 2. ПЛЮСУЕМ новые значения к старым (используем .coerceAtLeast(0f), чтобы не уйти в минус)
        activityDao.insertActivity(
            entity.copy(
                consumedCalories = (entity.consumedCalories + consumedCalories).coerceAtLeast(0f),
                proteins = (entity.proteins + proteins).coerceAtLeast(0f),
                fats = (entity.fats + fats).coerceAtLeast(0f),
                carbs = (entity.carbs + carbs).coerceAtLeast(0f)
            )
        )
    }

    override suspend fun saveWeight(date: String, weight: Float) {
        val entity = activityDao.getActivityByDateOnce(date) ?: DailyActivityEntity(date = date)
        activityDao.insertActivity(entity.copy(weight = weight))
    }

    override suspend fun getLatestWeight(): Float {
        return activityDao.getLatestWeight() ?: 75f
    }

    override suspend fun saveLocationPoint(sessionId: Long, location: UserLocation) {
        // Маппим доменную точку в Entity прямо перед вставкой
        locationDao.insertPoint(location.toEntity(sessionId))
    }

    override fun getPointsForSession(sessionId: Long): Flow<List<UserLocation>> {
        return locationDao.getPointsForSession(sessionId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteSession(sessionId: Long) {
        locationDao.deleteSession(sessionId)
    }

    override suspend fun hasLocationPoints(sessionId: Long): Boolean {
        return locationDao.hasPoints(sessionId)
    }
}

