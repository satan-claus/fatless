package com.niked.fatless.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.niked.fatless.core.utils.Constants.PREFS_NAME
import com.niked.fatless.core.utils.Constants.PREF_LAST_WIDGET_REFRESH

class WidgetRefreshAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("WIDGET_DEBUG", "Клик по кнопке обновления зафиксирован")

        // 1. Пишем время строго в SharedPrefs
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        prefs.edit().putString(PREF_LAST_WIDGET_REFRESH, currentTime).apply()

        // 2. Системный пинок для пробития кэша Android (Интент на обновление всех виджетов FatLess)
        val intent = Intent(context, StepWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        context.sendBroadcast(intent)

        // 3. Дублируем принудительный запуск перерисовки Glance
        StepWidget().updateAll(context)
    }
}
