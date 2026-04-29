package com.niked.fatless.core.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.niked.fatless.R
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.core.utils.Constants
import com.niked.fatless.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.core.graphics.toColorInt

@AndroidEntryPoint
class StepService : Service(), SensorEventListener {

    @Inject lateinit var settings: AppSettings
    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
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

            // 1. Смена дня (00:00)
            if (settings.lastStepResetDate != today) {
                settings.stepBaseCount = totalStepsSinceBoot
                settings.lastStepResetDate = today
            }

            // 2. Инициализация при первом запуске
            if (settings.stepBaseCount <= 0) {
                settings.stepBaseCount = totalStepsSinceBoot
            }

            // 3. ЗАЩИТА ОТ ПЕРЕЗАГРУЗКИ (Ключевой момент из статьи!)
            // Если телефон перезагрузился, totalStepsSinceBoot станет меньше базы.
            // Мы сбрасываем базу в 0, чтобы продолжить отчет.
            if (totalStepsSinceBoot < settings.stepBaseCount) {
                settings.stepBaseCount = 0
            }

            val dailySteps = totalStepsSinceBoot - settings.stepBaseCount

            // Записываем результат, если он корректный
            if (dailySteps >= 0 && dailySteps > settings.todaySteps) {
                settings.todaySteps = dailySteps
                updateNotification(dailySteps)
            }
        }
    }

    private fun createNotification(steps: Int): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val goal = settings.stepGoal
        val progress = if (goal > 0) (steps * 100 / goal).coerceAtMost(100) else 0

        // 1. КЛИКАБЕЛЬНОСТЬ: Создаем намерение открыть приложение
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.STEP_CHANNEL_ID,
                Constants.STEP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_logo_top_bar_128)

        // 2. СБОРКА УВЕДОМЛЕНИЯ
        return NotificationCompat.Builder(this, Constants.STEP_CHANNEL_ID)
            .setContentTitle("FatLess: $steps / $goal шагов")
            .setContentText("Прогресс дня: $progress%")
            .setSmallIcon(R.drawable.ic_directions_walk_24)
            .setLargeIcon(largeIcon)
            // 🟢 НАШ ЗЕЛЕНЫЙ ЦВЕТ
            .setColor("#4CAF50".toColorInt())
            // Позволяет раскрасить элементы управления
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            // 3. ПРОГРЕСС-БАР
            .setProgress(100, progress, false)
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
