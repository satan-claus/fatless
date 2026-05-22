package com.niked.fatless.core.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.LifecycleService
import com.niked.fatless.R
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.ISettingsRepository
import com.niked.fatless.ui.MainActivity
import com.niked.fatless.widget.StepWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.glance.appwidget.updateAll
import com.niked.fatless.core.utils.Constants.PREFS_NAME
import com.niked.fatless.core.utils.Constants.PREF_LAST_WIDGET_REFRESH
import androidx.core.content.edit

@AndroidEntryPoint
class StepService : LifecycleService(), SensorEventListener {

    @Inject
    lateinit var settingsRepository: ISettingsRepository

    @Inject
    lateinit var activityRepository: IActivityRepository

    @Inject
    lateinit var logger: AppLogger

    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        logger.log(LogLevel.SYSTEM, "SERVICE", "StepService создан (onCreate)")

        createNotificationChannel()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FatLess:StepWakeLock")
        wakeLock?.acquire()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val notification = createNotification(settingsRepository.todaySteps, settingsRepository.currentManualSteps)

        // Принудительно обновляем виджет при старте сервиса (после перезагрузки или сна)
        serviceScope.launch {
            try {
                StepWidget().updateAll(applicationContext)
            } catch (e: Exception) {
                // Игнорируем, если виджет не вынесен на экран
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Constants.STEP_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(Constants.STEP_NOTIFICATION_ID, notification)
        }

        reRegisterSensor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action != null) {
            logger.log(LogLevel.SYSTEM, "SERVICE", "Получена команда: ${intent.action}")
        }

        when (intent?.action) {
            Constants.ACTION_START_MANUAL -> {
                settingsRepository.isManualTracking = true
                settingsRepository.manualBaseSteps = -1
                settingsRepository.currentManualSteps = 0
            }
            Constants.ACTION_STOP_MANUAL -> {
                settingsRepository.isManualTracking = false
            }
            Constants.ACTION_CLEAR_MANUAL -> {
                settingsRepository.currentManualSteps = 0
                settingsRepository.manualBaseSteps = -1
            }
        }
        reRegisterSensor()
        updateNotification(settingsRepository.todaySteps, settingsRepository.currentManualSteps)
        return START_STICKY
    }

    private fun reRegisterSensor() {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.unregisterListener(this)
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalStepsSinceBoot = event.values[0].toInt()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. СМЕНА ДНЯ
            if (settingsRepository.lastStepResetDate != today) {
                val yesterdayDate = settingsRepository.lastStepResetDate
                val yesterdaySteps = settingsRepository.todaySteps
                val yesterdayCalories = settingsRepository.todayBurnedCalories
                val yesterdayWeight = settingsRepository.userWeight
                val hourlyData = settingsRepository.todayHourlySteps

                logger.log(LogLevel.SYSTEM, "SERVICE", "Смена дня: $yesterdayDate -> $today. Шаги за вчера: $yesterdaySteps")

                if (yesterdayDate.isNotEmpty() && (yesterdaySteps > 0 || yesterdayCalories > 0)) {
                    serviceScope.launch {
                        try {
                            activityRepository.saveSteps(
                                date = yesterdayDate,
                                steps = yesterdaySteps,
                                burnedCalories = yesterdayCalories,
                                currentWeight = yesterdayWeight,
                                hourlySteps = hourlyData
                            )
                            logger.log(LogLevel.INFO, "DATABASE", "Вчерашние данные заархивированы")
                        } catch (e: Exception) {
                            logger.log(LogLevel.ERROR, "DATABASE", "Ошибка архивации вчерашнего дня: ${e.message}")
                        }
                    }
                }

                settingsRepository.stepBaseCount = totalStepsSinceBoot
                settingsRepository.lastStepResetDate = today
                settingsRepository.todaySteps = 0
                settingsRepository.todayBurnedCalories = 0f
                settingsRepository.todayHourlySteps = "0,0,0,0,0,0,0,0"
                if (!settingsRepository.isManualTracking) settingsRepository.manualBaseSteps = -1

                // Сразу обнуляем виджет утром
                serviceScope.launch {
                    try { StepWidget().updateAll(applicationContext) } catch (e: Exception) {}
                }
            }

            // 2. ИНИЦИАЛИЗАЦИЯ
            if (settingsRepository.stepBaseCount <= 0) {
                logger.log(LogLevel.SYSTEM, "SERVICE", "Инициализация stepBaseCount: $totalStepsSinceBoot")
                settingsRepository.stepBaseCount = totalStepsSinceBoot
            }
            if (totalStepsSinceBoot < settingsRepository.stepBaseCount) {
                logger.log(LogLevel.SYSTEM, "SENSOR", "Датчик сброшен или перезагружен. Корректировка базы.")
                settingsRepository.stepBaseCount = totalStepsSinceBoot - settingsRepository.todaySteps
            }

            // 3. РАСЧЕТ ДЕЛЬТЫ И ОБНОВЛЕНИЕ ДАННЫХ
            val dailySteps = totalStepsSinceBoot - settingsRepository.stepBaseCount
            if (dailySteps >= 0 && dailySteps != settingsRepository.todaySteps) {
                val delta = dailySteps - settingsRepository.todaySteps

                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val intervalIndex = (currentHour / 3).coerceIn(0, 7)

                val stepsArray = settingsRepository.todayHourlySteps.split(",")
                    .map { it.toIntOrNull() ?: 0 }
                    .toMutableList()

                if (stepsArray.size == 8) {
                    stepsArray[intervalIndex] += delta
                    settingsRepository.todayHourlySteps = stepsArray.joinToString(",")
                }

                val weight = settingsRepository.userWeight
                val met = settingsRepository.currentMetModifier
                val calorieIncrement = delta * weight * (met / 7000f)

                settingsRepository.todaySteps = dailySteps
                settingsRepository.todayBurnedCalories += calorieIncrement

                // ЖИВОЙ ПИНК ВИДЖЕТА: Обновляем каждые 10 шагов, чтобы пройти лимиты Android на IPC
                if (dailySteps % 10 == 0) {
                    // 1. Фиксируем время автоматического обновления строго через константы
                    val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    prefs.edit { putString(PREF_LAST_WIDGET_REFRESH, currentTime) }

                    // 2. Пинаем сам виджет
                    serviceScope.launch {
                        try {
                            StepWidget().updateAll(applicationContext)
                        } catch (e: Exception) {
                            // Игнорим, если виджет удален с экрана
                        }
                    }
                }
            } else if (dailySteps < 0) {
                logger.log(LogLevel.ERROR, "SENSOR", "Отрицательные шаги: dailySteps = $dailySteps. Проверь базу данных!")
            }

            // 4. РУЧНОЙ ЗАМЕР
            var currentManual = settingsRepository.currentManualSteps
            if (settingsRepository.isManualTracking) {
                if (settingsRepository.manualBaseSteps == -1 || totalStepsSinceBoot < settingsRepository.manualBaseSteps) {
                    settingsRepository.manualBaseSteps = totalStepsSinceBoot - settingsRepository.currentManualSteps
                }
                currentManual = totalStepsSinceBoot - settingsRepository.manualBaseSteps
                if (currentManual >= 0) settingsRepository.currentManualSteps = currentManual
            }

            updateNotification(settingsRepository.todaySteps, currentManual)
        }
    }

    private fun createNotification(daily: Int, manual: Int): Notification {
        val goal = settingsRepository.stepGoal
        val progress = if (goal > 0) (daily * 100 / goal).coerceAtMost(100) else 0

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (settingsRepository.isManualTracking) {
            "День: $daily | ЗАМЕР: $manual"
        } else {
            "Прогресс дня: $progress%"
        }

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_logo_notification)

        return NotificationCompat.Builder(this, Constants.STEP_CHANNEL_ID)
            .setContentTitle("FatLess: $daily / $goal")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_directions_walk_24_green)
            .setLargeIcon(largeIcon)
            .setColor("#4CAF50".toColorInt())
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
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
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(daily: Int, manual: Int) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.STEP_NOTIFICATION_ID, createNotification(daily, manual))
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        logger.log(LogLevel.SYSTEM, "SERVICE", "StepService уничтожен (onDestroy)")
        serviceScope.cancel()
        wakeLock?.release()
        sensorManager.unregisterListener(this)
    }
}

