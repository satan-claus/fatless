package com.niked.fatless.core.utils

object Constants {
    const val DATABASE_NAME = "fatless_db"
    const val DATABASE_VERSION = 2
    const val LOG_TAG = "Fatless LOG"

    // Настройки
    const val PREFS_NAME = "fatless_settings"
    const val PREF_IS_FIRST_LAUNCH = "pref_is_first_launch"
    const val PREF_USER_HEIGHT = "pref_user_height"
    const val PREF_USER_WEIGHT = "pref_user_weight"
    const val PREF_STEP_BASE_COUNT = "step_base_count"
    const val PREF_MANUAL_BASE_STEPS = "manual_base_steps"
    const val PREF_LAST_STEP_RESET_DATE = "last_step_reset_date"
    const val PREF_TODAY_STEPS = "today_steps"
    const val PREF_STEP_GOAL = "step_goal"
    const val PREF_IS_SOUND_ENABLED = "is_sound_enabled"
    const val PREF_SOUND_VOLUME = "pref_sound_volume"
    const val PREF_AUTO_FINISH_ON_GOAL = "auto_finish_on_goal"
    const val PREF_CURRENT_MANUAL_STEPS = "pref_current_manual_steps"
    const val PREF_IS_MANUAL_TRACKING = "pref_is_manual_tracking"

    // Actions для управления сервисом
    const val ACTION_START_MANUAL = "ACTION_START_MANUAL"
    const val ACTION_STOP_MANUAL = "ACTION_STOP_MANUAL"
    const val ACTION_CLEAR_MANUAL = "ACTION_CLEAR_MANUAL"

    // Уведомления
    const val STEP_CHANNEL_ID = "step_tracker_channel"
    const val STEP_CHANNEL_NAME = "Шагомер FatLess"
    const val STEP_NOTIFICATION_ID = 1001
}
