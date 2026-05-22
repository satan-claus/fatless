package com.niked.fatless.di

import com.niked.fatless.data.repository.ActivityRepositoryImpl
import com.niked.fatless.data.repository.ExerciseRepositoryImpl
import com.niked.fatless.data.repository.NutritionRepositoryImpl
import com.niked.fatless.data.repository.SettingsRepositoryImpl
import com.niked.fatless.data.repository.WorkoutRepositoryImpl
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.IExerciseRepository
import com.niked.fatless.domain.repository.INutritionRepository
import com.niked.fatless.domain.repository.ISettingsRepository
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
        activityRepositoryImpl: ActivityRepositoryImpl
    ): IActivityRepository

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): IExerciseRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        nutritionRepositoryImpl: NutritionRepositoryImpl
    ): INutritionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): ISettingsRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): IWorkoutRepository
}