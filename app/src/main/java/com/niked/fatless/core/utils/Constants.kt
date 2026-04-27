package com.niked.fatless.core.utils

object Constants {
    const val DATABASE_NAME = "fatless_db"
    const val DATABASE_VERSION = 1
    const val LOG_TAG = "Fatless LOG"

    // Настройки
    const val PREFS_NAME = "fatless_settings"
    const val PREF_STEP_BASE_COUNT = "step_base_count"
    const val PREF_MANUAL_BASE_STEPS = "manual_base_steps"
    const val PREF_LAST_STEP_RESET_DATE = "last_step_reset_date"
    const val PREF_IS_SOUND_ENABLED = "is_sound_enabled"
    const val PREF_AUTO_FINISH_ON_GOAL = "auto_finish_on_goal"

    // Уведомления
    const val STEP_CHANNEL_ID = "step_tracker_channel"
    const val STEP_CHANNEL_NAME = "Шагомер FatLess"
    const val STEP_NOTIFICATION_ID = 1001
}
