package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.ExerciseType
import kotlinx.coroutines.flow.Flow

interface IExerciseRepository {
    fun getExerciseTypes(): Flow<List<ExerciseType>>
}
