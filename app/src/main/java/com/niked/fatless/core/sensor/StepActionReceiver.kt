package com.niked.fatless.core.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.niked.fatless.core.data.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StepActionReceiver : BroadcastReceiver() {

    @Inject lateinit var settings: AppSettings

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_RESET_MANUAL") {
            // Сбрасываем ручную базу.
            // Сервис при следующем шаге увидит -1 и зафиксирует новое значение.
            settings.manualBaseSteps = -1
        }
    }
}
