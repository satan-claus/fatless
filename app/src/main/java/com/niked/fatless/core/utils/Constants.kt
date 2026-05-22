package com.niked.fatless.core.utils

object Constants {
    const val DATABASE_NAME = "fatless_db"
    const val DATABASE_VERSION = 2
    const val LOG_TAG = "Fatless LOG"
    const val LOG_FILE_NAME = "app_logs.txt"

    enum class LogLevel {
        INFO,
        ERROR,
        DEBUG,
        SYSTEM
    }

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
    const val PREF_CURRENT_MET = "pref_current_met"
    const val PREF_IS_MANUAL_TRACKING = "pref_is_manual_tracking"
    const val PREF_TODAY_BURNED_CALORIES = "pref_today_burned_calories"
    const val PREF_HOURLY_STEPS = "pref_today_hourly_steps"
    const val PREF_LAST_WIDGET_REFRESH = "pref_last_widget_refresh"


    const val FORCE_REFRESH_TRIGGER = "force_refresh_trigger"

    // Actions для управления сервисом
    const val ACTION_START_MANUAL = "ACTION_START_MANUAL"
    const val ACTION_STOP_MANUAL = "ACTION_STOP_MANUAL"
    const val ACTION_CLEAR_MANUAL = "ACTION_CLEAR_MANUAL"

    // Уведомления
    const val STEP_CHANNEL_ID = "step_tracker_channel"
    const val STEP_CHANNEL_NAME = "Шагомер FatLess"
    const val STEP_NOTIFICATION_ID = 1001

    // Константы для GPS трекера
    const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
    const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
    const val LOCATION_NOTIFICATION_ID = 5001
}
