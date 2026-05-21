package com.niked.fatless.core.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import com.niked.fatless.core.utils.toSessionId
import com.niked.fatless.data.local.dao.ShopDao
import com.niked.fatless.data.local.dao.ShoppingDao
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject lateinit var locationClient: ILocationClient
    @Inject lateinit var activityRepository: IActivityRepository
    @Inject lateinit var settingsRepository: ISettingsRepository
    @Inject lateinit var logger: AppLogger
    // Наш список покупок
    @Inject lateinit var shoppingDao: ShoppingDao
    // Наш справочник магазинов
    @Inject lateinit var shopDao: ShopDao

    private var lastStepCount: Int = 0
    private var lastSavedLocation: Location? = null
    private val notifiedShops = mutableMapOf<Int, Long>()

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
        // Фиксируем шаги на момент старта
        lastStepCount = settingsRepository.todaySteps

        val initialId = LocalDate.now().toSessionId()
        logger.log(LogLevel.SYSTEM, "GPS", "Старт записи. Текущий ID дня: $initialId")

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("FatLess Трекер")
            .setContentText("Запись маршрута активна...")
            .setSmallIcon(R.drawable.ic_directions_run_24dp)
            .setOngoing(true)
            .build()

        startForeground(Constants.LOCATION_NOTIFICATION_ID, notification)

        locationClient.getLocationUpdates(3000L) // Раз в 3 секунды для города — самое то
            .onStart { logger.log(LogLevel.SYSTEM, "GPS", ">>> Поток запрошен у системы") }
            .catch { e -> logger.log(LogLevel.ERROR, "GPS", ">>> КРИТИЧЕСКАЯ ОШИБКА ПОТОКА: ${e.message}") }
            .onEach { location ->
                // Логируем ВООБЩЕ всё, что прилетает от спутников
                logger.log(LogLevel.DEBUG, "GPS", "СЫРАЯ ТОЧКА: lat=${location.latitude}, acc=${location.accuracy}м")
                processLocation(location)
            }
            .launchIn(lifecycleScope)
    }

    private fun processLocation(location: Location) {
        // 1. ЛОГ ТОЧНОСТИ
        if (location.accuracy > 50) { // Для города 50м — это ок, 20м бывает жестковато
            logger.log(LogLevel.DEBUG, "GPS", "!!! МИМО: Низкая точность (${location.accuracy}м)")
            return
        }

        // 2. ЛОГ ДИСТАНЦИИ
        val lastLoc = lastSavedLocation
        if (lastLoc != null) {
            val distance = location.distanceTo(lastLoc)
            if (distance < 3f) {
                logger.log(LogLevel.DEBUG, "GPS", "!!! МИМО: Слишком малый сдвиг (${String.format("%.1f", distance)}м)")
                return
            }
        }

        // Если прошли фильтры
        lastSavedLocation = location
        logger.log(LogLevel.SYSTEM, "GPS", "✅ ТОЧКА ПРИНЯТА: Скорость=${location.speed * 3.6f} км/ч")

        val currentSteps = settingsRepository.todaySteps
        // Если шагов стало больше, чем было при прошлой точке - значит идем
        val hasSteps = currentSteps > lastStepCount
        lastStepCount = currentSteps

        val type = resolveActivityType(location.speed, hasSteps)

        val userLocation = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            speed = location.speed,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        // Динамический ID: 2026-05-10 -> 20260510
        val dynamicSessionId = LocalDate.now().toSessionId()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                activityRepository.saveLocationPoint(
                    sessionId = dynamicSessionId,
                    location = userLocation
                )
            } catch (e: Exception) {
                logger.log(LogLevel.ERROR, "GPS", "Ошибка БД: ${e.message}")
            }
        }

        updateNotification(location.speed)

        checkNearbyShops(location)
    }

    private fun resolveActivityType(speedMs: Float, hasSteps: Boolean): ActivityType {
        val speedKmH = speedMs * 3.6f
        return when {
            // Идем, если есть шаги и скорость не как у ракеты
            hasSteps && speedKmH < 15f -> ActivityType.WALK
            // Велик/Самокат: шагов нет, скорость средняя
            !hasSteps && speedKmH in 5f..25f -> ActivityType.BIKE
            // Транспорт: летим быстро
            speedKmH >= 25f -> ActivityType.TRANSPORT
            // Стоим
            else -> ActivityType.STAY
        }
    }

    private fun stopTracking() {
        logger.log(LogLevel.SYSTEM, "GPS", "Запись остановлена")
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
        val speedKmH = String.format(Locale.getDefault(), "%.1f", speedMs * 3.6f)
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("FatLess Трекер")
            .setContentText("Скорость: $speedKmH км/ч | Идёт запись...")
            .setSmallIcon(R.drawable.ic_directions_run_24dp)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(Constants.LOCATION_NOTIFICATION_ID, notification)
    }

    private fun checkNearbyShops(location: Location) {
        // Используем уже имеющийся в сервисе lifecycleScope
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 2. Берем все магазины из базы
                val allShops = shopDao.getAllShops()

                allShops.forEach { shop ->
                    val shopLoc = Location("").apply {
                        latitude = shop.latitude
                        longitude = shop.longitude
                    }

                    val distance = location.distanceTo(shopLoc)

                    // 3. Если мы в радиусе (например, 500м)
                    if (distance <= shop.radius) {
                        logger.log(LogLevel.DEBUG, "GPS", "Дистанция до ${shop.name}: ${distance.toInt()}м (Цель: ${shop.radius}м)")

                        val now = System.currentTimeMillis()
                        val lastNotified = notifiedShops[shop.id] ?: 0L

                        // Проверка: не беспокоили ли мы тебя этим магазом последние 20 минут?
                        if (now - lastNotified > 20 * 60 * 1000) {

                            // Проверяем, есть ли НЕКУПЛЕННЫЕ товары в категории этого магазина
                            // (Убедись, что в ShoppingDao есть метод getItemsForCategory)
                            val itemsToBuy = shoppingDao.getItemsForCategory(shop.category)

                            if (itemsToBuy.isNotEmpty()) {
                                // Формируем список товаров строкой через запятую
                                val itemsString = itemsToBuy.joinToString(", ") { it.name }

                                // ПИЛИКАЕМ!
                                sendShoppingAlert(shop.name, itemsString, shop.id)

                                notifiedShops[shop.id] = now
                                logger.log(LogLevel.SYSTEM, "GPS", "Напоминание по магазину ${shop.name}: $itemsString")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.log(LogLevel.ERROR, "GPS", "Ошибка в дозоре магазинов: ${e.message}")
            }
        }
    }

    private fun sendShoppingAlert(shopName: String, items: String, shopId: Int) {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Рядом $shopName")
            .setContentText("Купить: $items")
            .setSmallIcon(R.drawable.ic_shopping_cart_24dp)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Используем shopId, чтобы уведомления от разных магазов не затирали друг друга
        manager.notify(shopId + 100, notification)
    }
}
