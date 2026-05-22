package com.niked.fatless.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.niked.fatless.domain.location.ILocationClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val locationManager: LocationManager
): ILocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                throw ILocationClient.LocationException("GPS/Network disabled")
            }

            // Создаем единый лисенер для всех провайдеров
            val listener = LocationListener { location ->
                launch { send(location) }
            }

            // ЗАПРОС ПО GPS (Основной)
            // 0f, чтобы система отдавала абсолютно ВСЕ точки во времени.
            // Фильтровать дистанцию будем в TrackingService.
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    interval,
                    // Забираем всё без ограничений по метрам
                    0f,
                    listener
                )
            }

            // ЗАПРОС ПО СЕТИ (Страховка для транспорта/помещений)
            // Если GPS отвалится в глухом лесу или вагоне, вышки связи подстрахуют
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    interval,
                    0f,
                    listener
                )
            }

            awaitClose {
                // Чистим за собой при закрытии потока
                locationManager.removeUpdates(listener)
            }
        }
    }
}