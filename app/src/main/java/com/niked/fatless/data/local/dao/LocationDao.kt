package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.niked.fatless.data.local.entities.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insertPoint(point: LocationEntity)

    // Получить все точки конкретной сессии (для отрисовки на карте)
    @Query("SELECT * FROM location_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSession(sessionId: Long): Flow<List<LocationEntity>>

    // Удалить старые треки (например, старше месяца), если захотим чистить
    @Query("DELETE FROM location_points WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
