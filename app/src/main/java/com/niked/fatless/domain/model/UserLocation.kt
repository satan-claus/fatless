package com.niked.fatless.domain.model

import org.osmdroid.util.GeoPoint

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val type: ActivityType, // Наш Enum с цветами
    val timestamp: Long
) {
    // Вспомогательный метод, чтобы сразу отдавать точку для карты
    fun toGeoPoint() = GeoPoint(latitude, longitude)
}