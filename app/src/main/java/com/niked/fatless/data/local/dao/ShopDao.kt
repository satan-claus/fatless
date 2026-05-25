package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.niked.fatless.data.local.entities.ShopEntity

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops ORDER BY id DESC")
    fun getAllShopsFlow(): kotlinx.coroutines.flow.Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops")
    suspend fun getAllShops(): List<ShopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Query("DELETE FROM shops WHERE id = :id")
    suspend fun deleteShop(id: Int)
}
