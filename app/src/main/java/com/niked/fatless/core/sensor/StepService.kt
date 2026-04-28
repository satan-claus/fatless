package com.niked.fatless.core.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.core.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class StepService : Service(), SensorEventListener {

    @Inject lateinit var settings: AppSettings
    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FatLess:StepWakeLock")
        wakeLock?.acquire()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Регистрируем датчик.
        // Важно: на многих девайсах одометр по определению отдает шаги с задержкой (латентностью)
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)

        startForeground(Constants.STEP_NOTIFICATION_ID, createNotification(settings.todaySteps))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalStepsSinceBoot = event.values[0].toInt()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Проверка на смену дня
            if (settings.lastStepResetDate != today) {
                settings.stepBaseCount = totalStepsSinceBoot
                settings.lastStepResetDate = today
                settings.todaySteps = 0
            }

            // 2. Инициализация (самый первый запуск)
            if (settings.stepBaseCount <= 0) {
                settings.stepBaseCount = totalStepsSinceBoot
            }

            // 3. Прямой расчет
            val dailySteps = totalStepsSinceBoot - settings.stepBaseCount

            // Записываем только прогресс (никаких фильтров скорости, верим железу)
            if (dailySteps > settings.todaySteps) {
                settings.todaySteps = dailySteps
                updateNotification(dailySteps)
            }
        }
    }

    private fun createNotification(steps: Int): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        return NotificationCompat.Builder(this, Constants.STEP_CHANNEL_ID)
            .setContentTitle("FatLess")
            .setContentText("Пройдено за день: $steps")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = createNotification(steps)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.STEP_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        sensorManager.unregisterListener(this)
    }
}
