package com.niked.fatless.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.niked.fatless.core.utils.Constants.PREFS_NAME
import com.niked.fatless.core.utils.Constants.PREF_AUTO_FINISH_ON_GOAL
import com.niked.fatless.core.utils.Constants.PREF_CURRENT_MANUAL_STEPS
import com.niked.fatless.core.utils.Constants.PREF_CURRENT_MET
import com.niked.fatless.core.utils.Constants.PREF_HOURLY_STEPS
import com.niked.fatless.core.utils.Constants.PREF_IS_FIRST_LAUNCH
import com.niked.fatless.core.utils.Constants.PREF_IS_MANUAL_TRACKING
import com.niked.fatless.core.utils.Constants.PREF_IS_SOUND_ENABLED
import com.niked.fatless.core.utils.Constants.PREF_LAST_STEP_RESET_DATE
import com.niked.fatless.core.utils.Constants.PREF_MANUAL_BASE_STEPS
import com.niked.fatless.core.utils.Constants.PREF_SOUND_VOLUME
import com.niked.fatless.core.utils.Constants.PREF_STEP_BASE_COUNT
import com.niked.fatless.core.utils.Constants.PREF_STEP_GOAL
import com.niked.fatless.core.utils.Constants.PREF_TODAY_BURNED_CALORIES
import com.niked.fatless.core.utils.Constants.PREF_TODAY_STEPS
import com.niked.fatless.core.utils.Constants.PREF_USER_HEIGHT
import com.niked.fatless.core.utils.Constants.PREF_USER_WEIGHT
import com.niked.fatless.domain.repository.ISettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : ISettingsRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var isFirstLaunch: Boolean
        get() = prefs.getBoolean(PREF_IS_FIRST_LAUNCH, true)
        set(value) = prefs.edit { putBoolean(PREF_IS_FIRST_LAUNCH, value) }

    override var userHeight: Int
        get() = prefs.getInt(PREF_USER_HEIGHT, 175)
        set(value) = prefs.edit { putInt(PREF_USER_HEIGHT, value) }

    override var userWeight: Float
        get() = prefs.getFloat(PREF_USER_WEIGHT, 75.0f)
        set(value) = prefs.edit { putFloat(PREF_USER_WEIGHT, value) }

    override var stepBaseCount: Int
        get() = prefs.getInt(PREF_STEP_BASE_COUNT, -1)
        set(value) = prefs.edit { putInt(PREF_STEP_BASE_COUNT, value) }

    override var manualBaseSteps: Int
        get() = prefs.getInt(PREF_MANUAL_BASE_STEPS, -1)
        set(value) = prefs.edit { putInt(PREF_MANUAL_BASE_STEPS, value) }

    override var lastStepResetDate: String
        get() = prefs.getString(PREF_LAST_STEP_RESET_DATE, "") ?: ""
        set(value) = prefs.edit { putString(PREF_LAST_STEP_RESET_DATE, value) }

    override var todaySteps: Int
        get() = prefs.getInt(PREF_TODAY_STEPS, 0)
        set(value) = prefs.edit { putInt(PREF_TODAY_STEPS, value) }

    override var stepGoal: Int
        get() = prefs.getInt(PREF_STEP_GOAL, 10000)
        set(value) = prefs.edit { putInt(PREF_STEP_GOAL, value) }

    override var currentManualSteps: Int
        get() = prefs.getInt(PREF_CURRENT_MANUAL_STEPS, 0)
        set(value) = prefs.edit { putInt(PREF_CURRENT_MANUAL_STEPS, value) }

    override var isManualTracking: Boolean
        get() = prefs.getBoolean(PREF_IS_MANUAL_TRACKING, false)
        set(value) = prefs.edit { putBoolean(PREF_IS_MANUAL_TRACKING, value) }

    override var isSoundEnabled: Boolean
        get() = prefs.getBoolean(PREF_IS_SOUND_ENABLED, true)
        set(value) = prefs.edit { putBoolean(PREF_IS_SOUND_ENABLED, value) }

    override var soundVolume: Float
        get() = prefs.getFloat(PREF_SOUND_VOLUME, 1.0f)
        set(value) = prefs.edit { putFloat(PREF_SOUND_VOLUME, value) }

    override var autoFinishOnGoal: Boolean
        get() = prefs.getBoolean(PREF_AUTO_FINISH_ON_GOAL, false)
        set(value) = prefs.edit { putBoolean(PREF_AUTO_FINISH_ON_GOAL, value) }

    override var currentMetModifier: Float
        get() = prefs.getFloat(PREF_CURRENT_MET, 3.5f)
        set(value) = prefs.edit { putFloat(PREF_CURRENT_MET, value) }

    override var todayBurnedCalories: Float
        get() = prefs.getFloat(PREF_TODAY_BURNED_CALORIES, 0f)
        set(value) = prefs.edit { putFloat(PREF_TODAY_BURNED_CALORIES, value) }

    override var todayHourlySteps: String
        get() = prefs.getString(PREF_HOURLY_STEPS, "0,0,0,0,0,0,0,0") ?: "0,0,0,0,0,0,0,0"
        set(value) = prefs.edit { putString(PREF_HOURLY_STEPS, value) }

    override fun observeSteps(onStepsChanged: (Int) -> Unit): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == PREF_TODAY_STEPS) {
                onStepsChanged(prefs.getInt(key, 0))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    override fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun clearAll() = prefs.edit { clear() }
}
