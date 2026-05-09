package com.niked.fatless.domain.repository

import android.content.SharedPreferences

interface ISettingsRepository {
    var isFirstLaunch: Boolean
    var userHeight: Int
    var userWeight: Float
    var stepBaseCount: Int
    var manualBaseSteps: Int
    var lastStepResetDate: String
    var todaySteps: Int
    var stepGoal: Int
    var currentManualSteps: Int
    var isManualTracking: Boolean
    var isSoundEnabled: Boolean
    var soundVolume: Float
    var autoFinishOnGoal: Boolean
    var currentMetModifier: Float
    var todayBurnedCalories: Float
    // Храним как "0,0,500,..."
    var todayHourlySteps: String

    fun observeSteps(onStepsChanged: (Int) -> Unit): SharedPreferences.OnSharedPreferenceChangeListener

    // Методы для работы со слушателями (чтобы ViewModel могла следить за шагами)
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
    fun clearAll()
}