package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.ShoppingDao
import com.niked.fatless.data.mapper.ShoppingMapper
import com.niked.fatless.domain.model.ShoppingItem
import com.niked.fatless.domain.repository.IShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingRepositoryImpl(
    private val shoppingDao: ShoppingDao
) : IShoppingRepository {

    override fun getAllItems(): Flow<List<ShoppingItem>> {
        return shoppingDao.getAllItemsFlow().map { entities ->
            ShoppingMapper.mapToDomainList(entities)
        }
    }

    override suspend fun insertItem(item: ShoppingItem) {
        shoppingDao.insertProduct(ShoppingMapper.mapToEntity(item))
    }

    override suspend fun deleteItem(item: ShoppingItem) {
        shoppingDao.deleteProduct(ShoppingMapper.mapToEntity(item))
    }

    override suspend fun updateCompletionStatus(id: Int, isCompleted: Boolean, completedAt: Long?) {
        shoppingDao.updateCompletionStatus(id, isCompleted, completedAt)
    }
}
