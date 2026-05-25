package com.niked.fatless.core.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import com.niked.fatless.ui.MainActivity
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
    @Inject lateinit var shoppingDao: ShoppingDao
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
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        lastStepCount = settingsRepository.todaySteps

        val initialId = LocalDate.now().toSessionId()
        logger.log(LogLevel.SYSTEM, "GPS", "Старт записи. Текущий ID дня: $initialId")

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("FatLess Трекер")
            .setContentText("Запись маршрута активна...")
            .setSmallIcon(R.drawable.ic_directions_run_24dp)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(Constants.LOCATION_NOTIFICATION_ID, notification)

        locationClient.getLocationUpdates(3000L)
            .onStart { logger.log(LogLevel.SYSTEM, "GPS", ">>> Поток запрошен у системы") }
            .catch { e -> logger.log(LogLevel.ERROR, "GPS", ">>> КРИТИЧЕСКАЯ ОШИБКА ПОТОКА: ${e.message}") }
            .onEach { location ->
                logger.log(LogLevel.DEBUG, "GPS", "СЫРАЯ ТОЧКА: lat=${location.latitude}, acc=${location.accuracy}м")
                processLocation(location)
            }
            .launchIn(lifecycleScope)
    }

    private fun processLocation(location: Location) {
        // Вычисляем текущую скорость в км/ч.
        // Если системная скорость кривая или 0, подстрахуемся расчетом по времени и дистанции,
        // но для фильтрации берем базовое значение км/ч
        val currentSpeedKmH = location.speed * 3.6f
        // Признак того, что мы в транспорте/поезде
        val isFastMovement = currentSpeedKmH > 15f

        // 1. АДАПТИВНЫЙ ФИЛЬТР ТОЧНОСТИ
        // В поезде/машине расширяем шлюз до 100 метров, чтобы не терять трек из-за экранирования вагона.
        // Пешком держим строгие 30 метров, чтобы не было "дрейфа" на месте.
        val maxAllowedAccuracy = if (isFastMovement) 100f else 30f

        if (location.accuracy > maxAllowedAccuracy) {
            logger.log(LogLevel.DEBUG, "GPS", "!!! МИМО: Низкая точность (${location.accuracy}м > лимита ${maxAllowedAccuracy}м)")
            return
        }

        // 2. АДАПТИВНЫЙ ФИЛЬТР ДИСТАНЦИИ
        val lastLoc = lastSavedLocation
        if (lastLoc != null) {
            val distance = location.distanceTo(lastLoc)
            // Если летим на поезде, нет смысла писать точки каждые 3 метра (забьем базу). Пишем от 15 метров.
            // Если стоим/идем — пишем от 3 метров.
            val minRequiredDistance = if (isFastMovement) 15f else 3f

            if (distance < minRequiredDistance) {
                logger.log(LogLevel.DEBUG, "GPS", "!!! МИМО: Малый сдвиг (${String.format("%.1f", distance)}м < требуемых ${minRequiredDistance}м)")
                return
            }
        }

        // Если прошли фильтры — фиксируем точку
        lastSavedLocation = location
        logger.log(LogLevel.SYSTEM, "GPS", "ТОЧКА ПРИНЯТА: Скорость=$currentSpeedKmH км/ч, Точность=${location.accuracy}м")

        val currentSteps = settingsRepository.todaySteps
        val hasSteps = currentSteps > lastStepCount
        lastStepCount = currentSteps

        val type = resolveActivityType(location, hasSteps)

        val userLocation = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            speed = location.speed,
            type = type,
            timestamp = System.currentTimeMillis()
        )

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

    private fun resolveActivityType(location: Location, hasSteps: Boolean): ActivityType {
        // Берем системную скорость
        var speedKmH = location.speed * 3.6f

        // ПОДСТРАХОВКА: Считаем реальную скорость по дельте времени и расстояния
        val lastLoc = lastSavedLocation
        if (lastLoc != null) {
            val distanceMeters = location.distanceTo(lastLoc)
            val timeSeconds = (location.time - lastLoc.time) / 1000f

            // Защита от деления на ноль
            if (timeSeconds > 0.5f) {
                val calculatedSpeedKmH = (distanceMeters / timeSeconds) * 3.6f

                // Если системная скорость равна 0 (GPS затупил в вагоне),
                // или она в 2 раза отличается от математически расчетной — верим математике
                if (speedKmH <= 0f || Math.abs(speedKmH - calculatedSpeedKmH) > speedKmH * 0.5f) {
                    speedKmH = calculatedSpeedKmH
                }
            }
        }

        // ОГРАНИЧИТЕЛЬ ХАОСА
        // Физический предел обычного поезда/автомобиля в маршруте — ну вряд ли больше 140 км/ч.
        // Если цифра улетает в космос, мягко гасим её до средней скорости транспорта.
        if (speedKmH > 150f) {
            speedKmH = 60f
        }

        // ОПРЕДЕЛЯЕМ АКТИВНОСТЬ ПО ЧЕСТНОЙ СКОРОСТИ
        return when {
            // Идем, если есть шаги и скорость адекватная
            hasSteps && speedKmH < 15f -> ActivityType.WALK
            // Велик/Самокат: шагов нет, скорость средняя
            !hasSteps && speedKmH in 5f..25f -> ActivityType.BIKE
            // Транспорт/Поезд: летим стабильно быстро
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
        logger.log(LogLevel.DEBUG, "GEOFENCE_TEST",
            "0.1. Проверяем точку G: latitude = ${location.latitude}; longitude = ${location.longitude}")
//        // ДОМАШНИЙ ТЕСТ: Жестко подменяем координаты на точку Пятёрочки
//        location.latitude = 51.595540
//        location.longitude = 45.696587
//
//        logger.log(LogLevel.DEBUG, "GEOFENCE_TEST",
//            "0.2. Проверяем точку G: latitude = ${location.latitude}; longitude = ${location.longitude}")
//
        // Используем Dispatchers.IO, так как идем в БД Room
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Берем все магазины из базы ( getAllShops() возвращает List<ShopEntity> )
                val allShops = shopDao.getAllShops()
                // ЛОГ 1: Проверяем, видит ли Room сам магазин в таблице
                logger.log(LogLevel.DEBUG, "GEOFENCE_TEST", "1. Магазинов в базе: ${allShops.size}")

                if (allShops.isEmpty()) return@launch

                val now = System.currentTimeMillis()

                allShops.forEach { shop ->
                    // Проверяем категорию напрямую
                    val hasItems = shoppingDao.hasItemsForCategory(shop.category)
                    // Если в этой категории всё куплено — скипаем магазин
                    if (!hasItems) return@forEach

                    // ЛОГ 2: Смотрим, какую категорию и какой ответ выдает база
                    logger.log(LogLevel.DEBUG, "GEOFENCE_TEST", "2. Магазин: ${shop.name}, Категория: '${shop.category}', Найдено продуктов в Room: $hasItems")

                    val shopLoc = Location("").apply {
                        latitude = shop.latitude
                        longitude = shop.longitude
                    }

                    // Вычисляем дистанцию
                    val distance = location.distanceTo(shopLoc)
                    // ФИНАЛЬНЫЙ РЕНТГЕН: Смотрим, какую дистанцию считает телефон на диване
                    logger.log(LogLevel.DEBUG, "GEOFENCE_TEST", "3. Магазин: ${shop.name}, Расчитанная дистанция: ${distance.toInt()} метров, Радиус лимита: ${shop.radius} метров")

                    // 2. Проверяем радиус (у каждого магазина свой из базы)
                    if (distance <= shop.radius) {
                        logger.log(LogLevel.DEBUG, "GPS", "Геозона! Дистанция до ${shop.name}: ${distance.toInt()}м")

                        val lastNotified = notifiedShops[shop.id] ?: 0L

                        // Анти-спам защита: напоминаем не чаще одного раза в 20 минут для конкретной точки
                        if (now - lastNotified > 20 * 60 * 1000) {

                            // 3. Достаем список НЕКУПЛЕННЫХ товаров конкретно под категорию этого магазина
                            val itemsToBuy = shoppingDao.getItemsForCategory(shop.category)

                            if (itemsToBuy.isNotEmpty()) {
                                // Собираем имена товаров (используем поле name)
                                val itemsString = itemsToBuy.joinToString(", ") { it.name }

                                // ПИЛИКАЕМ В ШТОРКУ!
                                sendShoppingAlert(shop.name, itemsString, shop.id)

                                // Фиксируем время пуша
                                notifiedShops[shop.id] = now
                                logger.log(LogLevel.SYSTEM, "GPS", "🔔 Сработал дозор [${shop.name}]: $itemsString")
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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, shopId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Рядом $shopName 🛒")
            .setContentText("Купить: $items")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Не забудьте купить:\n$items"))
            .setSmallIcon(R.drawable.ic_shopping_cart_24dp) // Убедись, что иконка тележки есть в drawable
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(500, 500, 500)) // Мягкая вибрация при приближении
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Сдвиг +100, чтобы не пересекаться с ID основного трекинг-уведомления
        manager.notify(shopId + 100, notification)
    }
}
