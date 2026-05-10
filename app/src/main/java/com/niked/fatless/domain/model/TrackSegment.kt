package com.niked.fatless.domain.model

import org.osmdroid.util.GeoPoint

sealed class TrackSegment(
    val points: List<GeoPoint>,
    val type: ActivityType
) {
    class Walk(points: List<GeoPoint>) : TrackSegment(points, ActivityType.WALK)
    class Bike(points: List<GeoPoint>) : TrackSegment(points, ActivityType.BIKE)
    class Transport(points: List<GeoPoint>) : TrackSegment(points, ActivityType.TRANSPORT)
    class Stay(points: List<GeoPoint>) : TrackSegment(points, ActivityType.STAY)

    companion object {
        fun create(type: ActivityType, points: List<GeoPoint>): TrackSegment {
            return when (type) {
                ActivityType.WALK -> Walk(points)
                ActivityType.BIKE -> Bike(points)
                ActivityType.TRANSPORT -> Transport(points)
                ActivityType.STAY -> Stay(points)
            }
        }
    }
}

