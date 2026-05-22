package com.niked.fatless.core.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.niked.fatless.domain.repository.ISettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StepActionReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsRepository: ISettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_RESET_MANUAL") {
            settingsRepository.manualBaseSteps = -1
            // ПИНАЕМ СЕРВИС, чтобы он перерисовал шторку
            val serviceIntent = Intent(context, StepService::class.java).apply {
                action = "ACTION_REFRESH_NOTIFICATION"
            }
            context.startService(serviceIntent)
        }
    }
}
