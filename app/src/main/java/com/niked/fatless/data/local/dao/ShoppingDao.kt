package com.niked.fatless.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.niked.fatless.data.local.entities.ShoppingListEntity

@Dao
interface ShoppingDao {

    @Query("SELECT EXISTS(SELECT 1 FROM shopping_list WHERE category = :category AND isCompleted = 0)")
    suspend fun hasItemsForCategory(category: String): Boolean

    @Query("SELECT * FROM shopping_list WHERE category = :category AND isCompleted = 0")
    suspend fun getItemsForCategory(category: String): List<ShoppingListEntity>
}