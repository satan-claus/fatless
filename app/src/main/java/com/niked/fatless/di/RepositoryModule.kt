package com.niked.fatless.di

import com.niked.fatless.data.repository.ActivityRepositoryImpl
import com.niked.fatless.data.repository.NutritionRepositoryImpl
import com.niked.fatless.data.repository.WorkoutRepositoryImpl
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.INutritionRepository
import com.niked.fatless.domain.repository.IWorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        impl: ActivityRepositoryImpl
    ): IActivityRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        nutritionRepositoryImpl: NutritionRepositoryImpl
    ): INutritionRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): IWorkoutRepository
}