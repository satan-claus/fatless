package com.niked.fatless.core.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.niked.fatless.R
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants
import com.niked.fatless.core.utils.Constants.ACTION_START_TRACKING
import com.niked.fatless.core.utils.Constants.ACTION_STOP_TRACKING
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.domain.location.ILocationClient
import com.niked.fatless.domain.model.ActivityType
import com.niked.fatless.domain.model.UserLocation
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.domain.repository.ISettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject lateinit var locationClient: ILocationClient
    @Inject lateinit var activityRepository: IActivityRepository
    @Inject lateinit var settingsRepository: ISettingsRepository
    @Inject lateinit var logger: AppLogger

    private var currentSessionId: Long = 0
    private var lastStepCount: Int = 0

    override fun onCreate() {
        super.onCreate()
        logger.log(LogLevel.SYSTEM, "GPS", "TrackingService создан")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTracking() {
        // Генерируем новый ID сессии на основе времени
        currentSessionId = System.currentTimeMillis()
        lastStepCount = settingsRepository.todaySteps

        logger.log(LogLevel.SYSTEM, "GPS", "Старт записи трека. Session: $currentSessionId")

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("FatLess Трекер")
            .setContentText("Запись маршрута активна...")
            .setSmallIcon(R.drawable.ic_directions_run_24dp)
            .setOngoing(true)
            .build()

        startForeground(Constants.LOCATION_NOTIFICATION_ID, notification)

        // Запускаем поток обновлений (раз в 5 секунд)
        locationClient.getLocationUpdates(5000L)
            .catch { e -> logger.log(LogLevel.ERROR, "GPS", "Ошибка потока: ${e.message}") }
            .onEach { location ->
                processLocation(location)
            }
            .launchIn(lifecycleScope)
    }

    private fun processLocation(location: Location) {
        val currentSteps = settingsRepository.todaySteps
        val hasSteps = currentSteps > lastStepCount
        lastStepCount = currentSteps

        // 1. Определяем тип активности (наш Enum)
        val type = resolveActivityType(location.speed, hasSteps)

        // 2. Создаем чистую доменную модель UserLocation
        val userLocation = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            speed = location.speed,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            // ВЫЗОВ РЕПОЗИТОРИЯ: передаем ID сессии и доменную модель
            activityRepository.saveLocationPoint(
                sessionId = currentSessionId,
                location = userLocation
            )
        }

        updateNotification(location.speed)
    }

    private fun resolveActivityType(speedMs: Float, hasSteps: Boolean): ActivityType {
        val speedKmH = speedMs * 3.6f
        return when {
            hasSteps && speedKmH < 15f -> ActivityType.WALK
            !hasSteps && speedKmH in 5f..25f -> ActivityType.BIKE
            speedKmH >= 25f -> ActivityType.TRANSPORT
            else -> ActivityType.STAY
        }
    }

    private fun stopTracking() {
        logger.log(LogLevel.SYSTEM, "GPS", "Запись трека остановлена")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "GPS Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(speedMs: Float) {
        val speedKmH = String.format("%.1f", speedMs * 3.6f)
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("FatLess Трекер")
            .setContentText("Скорость: $speedKmH км/ч | Идёт запись...")
            .setSmallIcon(R.drawable.ic_directions_run_24dp)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(Constants.LOCATION_NOTIFICATION_ID, notification)
    }
}