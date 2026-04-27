package com.niked.fatless.core.data

import android.content.Context
import android.content.SharedPreferences
import com.niked.fatless.core.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var stepBaseCount: Int
        get() = prefs.getInt(Constants.PREF_STEP_BASE_COUNT, -1)
        set(value) = prefs.edit { putInt(Constants.PREF_STEP_BASE_COUNT, value) }

    var manualBaseSteps: Int
        get() = prefs.getInt(Constants.PREF_MANUAL_BASE_STEPS, -1)
        set(value) = prefs.edit { putInt(Constants.PREF_MANUAL_BASE_STEPS, value) }

    var lastStepResetDate: String
        get() = prefs.getString(Constants.PREF_LAST_STEP_RESET_DATE, "") ?: ""
        set(value) = prefs.edit { putString(Constants.PREF_LAST_STEP_RESET_DATE, value) }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(Constants.PREF_IS_SOUND_ENABLED, true)
        set(value) = prefs.edit { putBoolean(Constants.PREF_IS_SOUND_ENABLED, value) }

    var autoFinishOnGoal: Boolean
        get() = prefs.getBoolean(Constants.PREF_AUTO_FINISH_ON_GOAL, true) // По умолчанию выключено
        set(value) = prefs.edit { putBoolean(Constants.PREF_AUTO_FINISH_ON_GOAL, value) }

    fun clearAll() = prefs.edit { clear() }
}
