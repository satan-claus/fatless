package com.niked.fatless.core.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d("FatLess_Boot", "Получено событие: $action")

        // Слушаем все варианты загрузки (холодная, быстрая, перезапуск)
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON") {

            // 1. Поднимаем наш основной сервис шагомера
            val serviceIntent = Intent(context, StepService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("FatLess_Boot", "Ошибка запуска сервиса: ${e.message}")
            }

            // 2. Сразу переподписываем WorkManager (наш пинатель)
            // Чтобы если сервис вдруг упадет, WorkManager про него вспомнил
            val restartWorkRequest = PeriodicWorkRequestBuilder<StepRestartWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "StepServiceRestart",
                ExistingPeriodicWorkPolicy.KEEP,
                restartWorkRequest
            )
        }
    }
}
