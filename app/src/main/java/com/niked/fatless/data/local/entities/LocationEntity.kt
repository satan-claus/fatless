package com.niked.fatless.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // ID на базе даты (например, 20260510)
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    // Скорость м/с
    val speed: Float,
    // Пишем сюда ActivityType.id (например, "WALK")
    val activityType: String,
    val timestamp: Long = System.currentTimeMillis()
)
