package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.ExerciseDao
import com.niked.fatless.data.mapper.toDomain
import com.niked.fatless.domain.model.ExerciseType
import com.niked.fatless.domain.repository.IExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : IExerciseRepository {
    override fun getExerciseTypes(): Flow<List<ExerciseType>> {
        return exerciseDao.getAllExerciseTypes().map { list ->
            list.map { it.toDomain() }
        }
    }
}
