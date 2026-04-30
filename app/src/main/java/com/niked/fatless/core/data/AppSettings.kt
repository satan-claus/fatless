package com.niked.fatless.core.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.niked.fatless.core.utils.Constants.PREFS_NAME
import com.niked.fatless.core.utils.Constants.PREF_AUTO_FINISH_ON_GOAL
import com.niked.fatless.core.utils.Constants.PREF_CURRENT_MANUAL_STEPS
import com.niked.fatless.core.utils.Constants.PREF_IS_FIRST_LAUNCH
import com.niked.fatless.core.utils.Constants.PREF_IS_MANUAL_TRACKING
import com.niked.fatless.core.utils.Constants.PREF_IS_SOUND_ENABLED
import com.niked.fatless.core.utils.Constants.PREF_LAST_STEP_RESET_DATE
import com.niked.fatless.core.utils.Constants.PREF_MANUAL_BASE_STEPS
import com.niked.fatless.core.utils.Constants.PREF_SOUND_VOLUME
import com.niked.fatless.core.utils.Constants.PREF_STEP_BASE_COUNT
import com.niked.fatless.core.utils.Constants.PREF_STEP_GOAL
import com.niked.fatless.core.utils.Constants.PREF_TODAY_STEPS
import com.niked.fatless.core.utils.Constants.PREF_USER_HEIGHT
import com.niked.fatless.core.utils.Constants.PREF_USER_WEIGHT

@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(PREF_IS_FIRST_LAUNCH, true)
        set(value) = prefs.edit { putBoolean(PREF_IS_FIRST_LAUNCH, value) }

    var userHeight: Int
        get() = prefs.getInt(PREF_USER_HEIGHT, 175) // 175 см по дефолту
        set(value) = prefs.edit { putInt(PREF_USER_HEIGHT, value) }

    var userWeight: Int
        get() = prefs.getInt(PREF_USER_WEIGHT, 75) // 75 кг по дефолту
        set(value) = prefs.edit { putInt(PREF_USER_WEIGHT, value) }

    var stepBaseCount: Int
        get() = prefs.getInt(PREF_STEP_BASE_COUNT, -1)
        set(value) = prefs.edit { putInt(PREF_STEP_BASE_COUNT, value) }

    var manualBaseSteps: Int
        get() = prefs.getInt(PREF_MANUAL_BASE_STEPS, -1)
        set(value) = prefs.edit { putInt(PREF_MANUAL_BASE_STEPS, value) }

    var lastStepResetDate: String
        get() = prefs.getString(PREF_LAST_STEP_RESET_DATE, "") ?: ""
        set(value) = prefs.edit { putString(PREF_LAST_STEP_RESET_DATE, value) }

    // Текущие шаги за день (результат расчетов сервиса)
    var todaySteps: Int
        get() = prefs.getInt(PREF_TODAY_STEPS, 0)
        set(value) = prefs.edit { putInt(PREF_TODAY_STEPS, value) }

    // Цель на день (например, 10 000)
    var stepGoal: Int
        get() = prefs.getInt(PREF_STEP_GOAL, 10000)
        set(value) = prefs.edit { putInt(PREF_STEP_GOAL, value) }

    // Результат текущего активного замера
    var currentManualSteps: Int
        get() = prefs.getInt(PREF_CURRENT_MANUAL_STEPS, 0)
        set(value) = prefs.edit { putInt(PREF_CURRENT_MANUAL_STEPS, value) }

    // Флаг активного замера
    var isManualTracking: Boolean
        get() = prefs.getBoolean(PREF_IS_MANUAL_TRACKING, false)
        set(value) = prefs.edit { putBoolean(PREF_IS_MANUAL_TRACKING, value) }


    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(PREF_IS_SOUND_ENABLED, true)
        set(value) = prefs.edit { putBoolean(PREF_IS_SOUND_ENABLED, value) }

    var soundVolume: Float
        // По умолчанию 100%
        get() = prefs.getFloat(PREF_SOUND_VOLUME, 1.0f)
        set(value) = prefs.edit { putFloat(PREF_SOUND_VOLUME, value) }

    // По умолчанию выключено
    var autoFinishOnGoal: Boolean
        get() = prefs.getBoolean(PREF_AUTO_FINISH_ON_GOAL, false)
        set(value) = prefs.edit { putBoolean(PREF_AUTO_FINISH_ON_GOAL, value) }

    fun observeSteps(onStepsChanged: (Int) -> Unit): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == PREF_TODAY_STEPS) {
                onStepsChanged(prefs.getInt(key, 0))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    // Регистрация слушателя
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    // Отмена регистрации
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun clearAll() = prefs.edit { clear() }
}
