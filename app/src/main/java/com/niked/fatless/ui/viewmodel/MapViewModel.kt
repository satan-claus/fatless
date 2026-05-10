package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.TrackSegment
import com.niked.fatless.domain.model.UserLocation
import com.niked.fatless.domain.repository.IActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val activityRepository: IActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Вытаскиваем sessionId
    private val sessionId: Long = savedStateHandle["sessionId"] ?: 0L

    val trackSegments: StateFlow<List<TrackSegment>> = activityRepository
        .getPointsForSession(sessionId)
        .map { userLocations ->
            prepareSegments(userLocations)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    /**
     * Группируем точки в цветные сегменты
     * Если между точками > 5 минут — создаем новый сегмент (разрыв)
     */
    private fun prepareSegments(points: List<UserLocation>): List<TrackSegment> {
        if (points.isEmpty()) return emptyList()

        val segments = mutableListOf<TrackSegment>()
        var currentType = points.first().type
        var currentPoints = mutableListOf<GeoPoint>()
        var lastPoint: UserLocation? = null

        points.forEach { point ->
            val geoPoint = point.toGeoPoint()
            val timeGap = lastPoint?.let { point.timestamp - it.timestamp } ?: 0L
            // 5 минут разрыва
            val isNewSegment = timeGap > 5 * 60 * 1000

            if (isNewSegment || point.type != currentType) {
                // Завершаем старый сегмент
                if (currentPoints.isNotEmpty()) {
                    segments.add(TrackSegment.create(currentType, currentPoints.toList()))
                }
                // Начинаем новый
                currentType = point.type
                currentPoints = mutableListOf(geoPoint)
            } else {
                currentPoints.add(geoPoint)
            }
            lastPoint = point
        }

        // Добавляем последний кусок
        if (currentPoints.isNotEmpty()) {
            segments.add(TrackSegment.create(currentType, currentPoints.toList()))
        }

        return segments
    }
}
