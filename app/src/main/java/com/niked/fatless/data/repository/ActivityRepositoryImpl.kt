package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.ActivityDao
import com.niked.fatless.data.local.entities.DailyActivityEntity
import com.niked.fatless.domain.repository.IActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao
) : IActivityRepository {

    override fun getActivityHistory(): Flow<List<DailyActivityEntity>> {
        return activityDao.getActivityHistory()
    }

    override suspend fun saveSteps(date: String, steps: Int) {
        val current = activityDao.getActivityByDate(date) ?: DailyActivityEntity(date)
        activityDao.insertActivity(current.copy(steps = steps))
    }

    override suspend fun saveNutrition(date: String, cal: Int, p: Float, f: Float, c: Float) {
        val current = activityDao.getActivityByDate(date) ?: DailyActivityEntity(date)
        activityDao.insertActivity(
            current.copy(
                calories = (current.calories + cal).coerceAtLeast(0),
                proteins = (current.proteins + p).coerceAtLeast(0f),
                fats = (current.fats + f).coerceAtLeast(0f),
                carbs = (current.carbs + c).coerceAtLeast(0f)
            )
        )
    }
}
