package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.niked.fatless.data.local.entities.FoodCategoryEntity
import com.niked.fatless.data.local.entities.FoodDiaryEntity
import com.niked.fatless.data.local.entities.FoodEntity
import com.niked.fatless.data.local.relation.FoodWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // --- КАТЕГОРИИ ---
    @Query("SELECT * FROM food_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<FoodCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: FoodCategoryEntity)

    // --- СПРАВОЧНИК ПРОДУКТОВ (FoodEntity) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: FoodEntity): Long

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getProductById(id: String): FoodEntity?

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<FoodEntity>>

    // --- СЛОЖНЫЕ ЗАПРОСЫ (JOIN с категориями) ---
    @Transaction
    @Query("""
        SELECT food_items.*, food_categories.name as categoryName 
        FROM food_items 
        INNER JOIN food_categories ON food_items.categoryId = food_categories.categoryId
        WHERE food_items.name LIKE '%' || :query || '%'
    """)
    fun searchProductsWithCategory(query: String): Flow<List<FoodWithCategory>>

    @Transaction
    @Query("""
        SELECT food_items.*, food_categories.name as categoryName 
        FROM food_items 
        INNER JOIN food_categories ON food_items.categoryId = food_categories.categoryId
        WHERE food_items.id = :id
    """)
    suspend fun getProductWithCategoryById(id: String): FoodWithCategory?

    // --- ДНЕВНИК ПИТАНИЯ (FoodDiaryEntity) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: FoodDiaryEntity): Long

    @Query("SELECT * FROM food_diary WHERE dateTimestamp BETWEEN :startOfDay AND :endOfDay")
    fun getDiaryEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodDiaryEntity>>

    @Query("DELETE FROM food_diary WHERE entryId = :id")
    suspend fun deleteDiaryEntryById(id: Long)

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getFoodCount(): Int
}
