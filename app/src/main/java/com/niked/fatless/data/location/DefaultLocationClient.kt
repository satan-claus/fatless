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

            val listener = LocationListener { location ->
                launch { send(location) }
            }

            // Запрашиваем обновления по GPS
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                interval,
                5f, // Минимум 5 метров между точками
                listener
            )

            // Закрываем лавочку, когда Flow прекращает работу
            awaitClose {
                locationManager.removeUpdates(listener)
            }
        }
    }
}