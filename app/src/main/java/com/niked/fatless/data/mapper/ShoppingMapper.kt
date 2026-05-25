package com.niked.fatless.data.mapper

import com.niked.fatless.data.local.entities.ShoppingListEntity
import com.niked.fatless.domain.model.ShoppingItem

object ShoppingMapper {

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
}
