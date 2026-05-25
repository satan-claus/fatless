package com.niked.fatless.data.repository

import com.niked.fatless.data.local.dao.FoodDao
import com.niked.fatless.data.local.dao.ShopDao
import com.niked.fatless.data.local.dao.ShoppingDao
import com.niked.fatless.data.mapper.ShoppingMapper
import com.niked.fatless.domain.model.FoodItem
import com.niked.fatless.domain.model.Shop
import com.niked.fatless.domain.model.ShoppingItem
import com.niked.fatless.domain.repository.IShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao,
    private val shopDao: ShopDao,
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

    override fun getAllShops(): Flow<List<Shop>> {
        // Получаем реактивный стрим из базы и мапим каждую сущность в доменную модель
        return shopDao.getAllShopsFlow().map { entities ->
            ShoppingMapper.mapToDomainShopList(entities)
        }
    }

    override suspend fun insertShop(shop: Shop) {
        // Конвертируем чистую модель во внутреннюю Entity Room и пишем в базу
        shopDao.insertShop(ShoppingMapper.mapToEntity(shop))
    }

    override suspend fun deleteShop(shop: Shop) {
        shopDao.deleteShop(shop.id)
    }

    override fun getAvailableFoodItems(): Flow<List<FoodItem>> {
        return foodDao.searchProductsWithCategory("").map { list ->
            list.map { FoodWithCategory ->
                ShoppingMapper.mapToDomain(FoodWithCategory)
            }
        }
    }
}
