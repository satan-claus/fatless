package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.domain.model.Shop
import com.niked.fatless.domain.model.ShoppingItem
import com.niked.fatless.domain.repository.IShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val foodRepository: IShoppingRepository,
    private val shoppingRepository: IShoppingRepository
) : ViewModel() {

    val availableCategories = foodRepository.getAvailableFoodItems()
        .map { foodList ->
            foodList.map { it.categoryName }.distinct().filter { it.isNotBlank() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableFoods = foodRepository.getAvailableFoodItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 1. Стрим данных для Compose. Автоматически обновляет UI при любых изменениях в Room
    val shoppingItems = shoppingRepository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shopItems = shoppingRepository.getAllShops()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // 2. Метод добавления нового товара
    fun addItem(name: String, category: String, foodId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = ShoppingItem(
                id = 0,
                foodId = foodId,
                name = name,
                category = category,
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                completedAt = null
            )
            shoppingRepository.insertItem(newItem)
        }
    }

    // 3. Метод удаления товара из списка
    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.deleteItem(item)
        }
    }

    // 4. Метод клика по чекбоксу с фиксацией точного времени completedAt
    fun toggleItemCompletion(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCompletedState = !item.isCompleted
            // Если ставим галочку — пишем таймстамп, если снимаем — зануляем поле
            val completedAtTimestamp = if (newCompletedState) System.currentTimeMillis() else null

            shoppingRepository.updateCompletionStatus(
                id = item.id,
                isCompleted = newCompletedState,
                completedAt = completedAtTimestamp
            )
        }
    }

    fun addShop(name: String, categories: List<String>, radius: Float, lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newShop = Shop(
                id = 0,
                name = name,
                categories = categories,
                radius = radius,
                latitude = lat,
                longitude = lon
            )
            shoppingRepository.insertShop(newShop)
        }
    }

    fun deleteShop(shop: Shop) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.deleteShop(shop)
        }
    }
}
