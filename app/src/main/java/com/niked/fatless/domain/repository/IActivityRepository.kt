package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface IActivityRepository {
    fun getActivityHistory(): Flow<List<DailyActivity>>
    fun getActivityForMonth(month: String): Flow<List<DailyActivity>>

    suspend fun saveSteps(
        date: String,
        steps: Int,
        burnedCalories: Float,
        currentWeight: Float,
        hourlySteps: String
    )
    suspend fun saveNutrition(
        date: String,
        consumedCalories: Float,
        proteins: Float,
        fats: Float,
        carbs: Float
    )
    suspend fun saveWeight(date: String, weight: Float)
    suspend fun getLatestWeight(): Float

    suspend fun saveLocationPoint(sessionId: Long, location: UserLocation)
    fun getPointsForSession(sessionId: Long): Flow<List<UserLocation>>
    suspend fun deleteSession(sessionId: Long)
    suspend fun hasLocationPoints(sessionId: Long): Boolean
}