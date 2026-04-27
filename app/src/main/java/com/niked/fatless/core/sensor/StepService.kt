package com.niked.fatless.core.sensor

import android.app.*
import android.content.*
import android.hardware.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.core.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class StepService : Service(), SensorEventListener {

    @Inject lateinit var settings: AppSettings
    private lateinit var sensorManager: SensorManager
    private var lastTotalSteps = 0 // Храним последнее значение датчика

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_REFRESH_NOTIFICATION") {
            // Если пришла команда на сброс - принудительно обновляем
            refreshCounters()
        }
        return START_STICKY
    }

    private fun refreshCounters() {
        if (lastTotalSteps == 0) return

        // Считаем актуальные цифры
        val daily = lastTotalSteps - settings.stepBaseCount
        if (settings.manualBaseSteps == -1) settings.manualBaseSteps = lastTotalSteps
        val manual = lastTotalSteps - settings.manualBaseSteps

        updateNotification(daily, manual)
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)

        // Начальный запуск
        startForeground(Constants.STEP_NOTIFICATION_ID, createNotification(0, 0))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            lastTotalSteps = event.values[0].toInt()

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Если наступил новый день — сбрасываем базу
            if (settings.lastStepResetDate != today) {
                settings.stepBaseCount = lastTotalSteps
                settings.lastStepResetDate = today
            }

            // 2. КРИТИЧНО: Если это вообще самый первый запуск (база еще -1)
            if (settings.stepBaseCount == -1) {
                settings.stepBaseCount = lastTotalSteps
            }

            // Вся остальная логика (расчет daily, manual и апдейт шторки) теперь тут:
            refreshCounters()
        }
    }

    private fun createNotification(daily: Int, manual: Int): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.STEP_CHANNEL_ID,
                Constants.STEP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }

        // Настройка кнопки "Сбросить"
        val resetIntent = Intent(this, StepActionReceiver::class.java).apply {
            action = "ACTION_RESET_MANUAL"
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            this, 1, resetIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, Constants.STEP_CHANNEL_ID)
            .setContentTitle("FatLess: Шагомер")
            .setContentText("За день: $daily | Замер: $manual")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .addAction(android.R.drawable.ic_menu_revert, "Сбросить замер", resetPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(daily: Int, manual: Int) {
        val notification = createNotification(daily, manual)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.STEP_NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
