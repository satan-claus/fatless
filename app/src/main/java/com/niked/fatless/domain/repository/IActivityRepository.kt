package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.DailyActivity
import kotlinx.coroutines.flow.Flow

interface IActivityRepository {
    fun getActivityHistory(): Flow<List<DailyActivity>>
    suspend fun saveSteps(date: String, steps: Int, currentWeight: Float)
    suspend fun saveNutrition(date: String, cal: Int, p: Float, f: Float, c: Float)
    fun getActivityForMonth(month: String): Flow<List<DailyActivity>>
}