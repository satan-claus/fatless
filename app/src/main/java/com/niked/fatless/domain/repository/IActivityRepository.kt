package com.niked.fatless.domain.repository

import com.niked.fatless.data.local.entities.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

interface IActivityRepository {
    fun getActivityHistory(): Flow<List<DailyActivityEntity>>
    suspend fun saveSteps(date: String, steps: Int)
    suspend fun saveNutrition(date: String, cal: Int, p: Float, f: Float, c: Float)
}