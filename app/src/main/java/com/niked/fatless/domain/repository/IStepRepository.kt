package com.niked.fatless.domain.repository

import com.niked.fatless.data.local.entities.StepHistoryEntity
import kotlinx.coroutines.flow.Flow

interface IStepRepository {
    fun getHistory(): Flow<List<StepHistoryEntity>>
    suspend fun saveToHistory(date: String, steps: Int)
}