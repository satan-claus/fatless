package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.StepDao
import com.niked.fatless.data.local.entities.StepHistoryEntity
import com.niked.fatless.domain.repository.IStepRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepositoryImpl @Inject constructor(
    private val stepDao: StepDao
) : IStepRepository {

    override fun getHistory(): Flow<List<StepHistoryEntity>> {
        return stepDao.getLastWeekHistory()
    }

    override suspend fun saveToHistory(date: String, steps: Int) {
        val entity = StepHistoryEntity(date = date, steps = steps)
        stepDao.insertSteps(entity)
    }
}
