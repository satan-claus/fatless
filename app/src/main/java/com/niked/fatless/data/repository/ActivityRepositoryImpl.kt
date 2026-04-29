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
        activityDao.insertActivity(current.copy(calories = cal, proteins = p, fats = f, carbs = c))
    }
}
