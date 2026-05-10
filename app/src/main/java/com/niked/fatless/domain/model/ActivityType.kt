package com.niked.fatless.domain.model

import androidx.compose.ui.graphics.Color
import com.niked.fatless.ui.theme.ColorTrackBike
import com.niked.fatless.ui.theme.ColorTrackTransport
import com.niked.fatless.ui.theme.ColorTrackWalk

enum class ActivityType(
    val id: String,
    val color: Color,
    val strokeWidth: Float
) {
    WALK("WALK", ColorTrackWalk, 12f),
    BIKE("BIKE", ColorTrackBike, 8f),
    TRANSPORT("BUS", ColorTrackTransport, 4f),
    STAY("STAY", Color.Transparent, 0f);

    companion object {
        fun fromId(id: String?): ActivityType {
            return entries.find { it.id == id } ?: STAY
        }
    }
}
