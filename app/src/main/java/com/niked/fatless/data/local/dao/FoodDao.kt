package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // Поиск по справочнику
    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<FoodEntity>>

    // Добавить продукт в справочник
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: FoodEntity)

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getProductById(id: String): FoodEntity?

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteProductById(id: String)

    // ЗАПИСЬ В ДНЕВНИК
    @Insert
    suspend fun insertDiaryEntry(entry: FoodDiaryEntity)

    // ПОЛУЧИТЬ КБЖУ ЗА ДЕНЬ (Для нашего кружка)
    @Query("SELECT * FROM food_diary WHERE dateTimestamp BETWEEN :startOfDay AND :endOfDay")
    fun getDiaryEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodDiaryEntity>>

    @Query("DELETE FROM food_diary WHERE entryId = :id")
    suspend fun deleteDiaryEntryById(id: Long)
}