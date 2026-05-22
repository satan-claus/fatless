package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface IWorkoutRepository {
    // Наблюдаем за списком всех тренировок (Flow для реактивности)
    fun observeAllWorkouts(): Flow<List<Workout>>

    // Получаем одну конкретную тренировку по ID
    suspend fun getWorkoutById(id: String): Workout?

    // Сохраняем (и воркаут, и интервалы сразу)
    suspend fun saveWorkout(workout: Workout)

    // Удаляем
    suspend fun deleteWorkout(id: String)

    // Набиваем базу дефолтными данными при первом запуске
    suspend fun initializeDefaultData()
}