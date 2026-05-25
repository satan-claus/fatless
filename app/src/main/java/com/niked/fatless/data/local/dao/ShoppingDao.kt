package com.niked.fatless.data.local.dao

import androidx.room.*
import com.niked.fatless.data.local.entities.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    // 🎯 ДОБАВЛЕНО: Стрим всех товаров для UI экрана
    @Query("SELECT * FROM shopping_list ORDER BY createdAt DESC")
    fun getAllItemsFlow(): Flow<List<ShoppingListEntity>>

    // 🎯 ДОБАВЛЕНО: Вставка товара
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(item: ShoppingListEntity)

    // 🎯 ДОБАВЛЕНО: Удаление товара
    @Delete
    suspend fun deleteProduct(item: ShoppingListEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM shopping_list WHERE category = :category AND isCompleted = 0)")
    suspend fun hasItemsForCategory(category: String): Boolean

    @Query("SELECT * FROM shopping_list WHERE category = :category AND isCompleted = 0")
    suspend fun getItemsForCategory(category: String): List<ShoppingListEntity>

    @Query("UPDATE shopping_list SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Int, isCompleted: Boolean, completedAt: Long?)
}
