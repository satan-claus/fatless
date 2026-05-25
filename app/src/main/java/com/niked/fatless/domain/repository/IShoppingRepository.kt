package com.niked.fatless.domain.repository

import com.niked.fatless.domain.model.Shop
import com.niked.fatless.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface IShoppingRepository {
    fun getAllItems(): Flow<List<ShoppingItem>>

    suspend fun insertItem(item: ShoppingItem)

    suspend fun deleteItem(item: ShoppingItem)

    suspend fun updateCompletionStatus(id: Int, isCompleted: Boolean, completedAt: Long?)

    fun getAllShops(): Flow<List<Shop>>

    suspend fun insertShop(shop: Shop)

    suspend fun deleteShop(shop: Shop)
}
