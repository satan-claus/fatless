package com.niked.fatless.domain.model

data class ShoppingItem(
    val id: Int,
    val foodId: String,
    val name: String,
    val category: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val completedAt: Long?
)