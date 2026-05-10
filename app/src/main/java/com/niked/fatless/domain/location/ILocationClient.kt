package com.niked.fatless.domain.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface ILocationClient {
    // Получение потока координат с заданным интервалом
    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationException(message: String): Exception(message)
}