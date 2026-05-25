package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.ShopEntity
import com.niked.fatless.data.local.entities.ShoppingListEntity
import com.niked.fatless.domain.model.Shop
import com.niked.fatless.domain.model.ShoppingItem

object ShoppingMapper {

    // --- 🍏 МАППИНГ ТОВАРОВ (ShoppingItem) ---
    fun mapToDomain(entity: ShoppingListEntity): ShoppingItem {
        return ShoppingItem(
            id = entity.id,
            foodId = entity.foodId,
            name = entity.name,
            category = entity.category,
            isCompleted = entity.isCompleted,
            createdAt = entity.createdAt,
            completedAt = entity.completedAt
        )
    }

    fun mapToEntity(domain: ShoppingItem): ShoppingListEntity {
        return ShoppingListEntity(
            id = domain.id,
            foodId = domain.foodId,
            name = domain.name,
            category = domain.category,
            isCompleted = domain.isCompleted,
            createdAt = domain.createdAt,
            completedAt = domain.completedAt
        )
    }

    fun mapToDomainList(entities: List<ShoppingListEntity>): List<ShoppingItem> {
        return entities.map { mapToDomain(it) }
    }

    // --- 🛒 МАППИНГ МАГАЗИНОВ (Shop) ---
    fun mapToDomain(entity: ShopEntity): Shop {
        return Shop(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            radius = entity.radius,
            latitude = entity.latitude,
            longitude = entity.longitude
        )
    }

    fun mapToEntity(domain: Shop): ShopEntity {
        return ShopEntity(
            id = domain.id,
            name = domain.name,
            category = domain.category,
            radius = domain.radius,
            latitude = domain.latitude,
            longitude = domain.longitude
        )
    }

    fun mapToDomainShopList(entities: List<ShopEntity>): List<Shop> {
        return entities.map { mapToDomain(it) }
    }
}
