package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.LocationEntity
import com.niked.fatless.domain.model.ActivityType
import com.niked.fatless.domain.model.UserLocation

/**
 * Маппер из базы в домен (для Карты)
 */
fun LocationEntity.toDomain(): UserLocation {
    return UserLocation(
        latitude = this.latitude,
        longitude = this.longitude,
        speed = this.speed,
        type = ActivityType.fromId(this.activityType),
        timestamp = this.timestamp
    )
}

/**
 * Маппер из домена в базу (для сохранения в TrackingService)
 */
fun UserLocation.toEntity(sessionId: Long): LocationEntity {
    return LocationEntity(
        sessionId = sessionId,
        latitude = this.latitude,
        longitude = this.longitude,
        speed = this.speed,
        activityType = this.type.id,
        timestamp = this.timestamp
    )
}