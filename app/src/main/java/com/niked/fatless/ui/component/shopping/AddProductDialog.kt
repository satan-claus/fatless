package com.niked.fatless.ui.component.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.domain.model.FoodItem

@Composable
fun AddProductDialog(
    foodItems: List<FoodItem>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, foodId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }

    // Фильтруем список на лету в фоновом режиме памяти
    val filteredFoodItems = remember(searchQuery, foodItems) {
        if (searchQuery.isBlank()) {
            foodItems
        } else {
            foodItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_product_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ЧИСТОЕ ПОЛЕ ВВОДА
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.dialog_select_food_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ОПТИМИЗАЦИЯ ТОРМОЗОВ: LazyColumn рендерит только видимые строки, ничего не виснет!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Жестко ограничиваем высоту контейнера
                ) {
                    items(items = filteredFoodItems, key = { it.id }) { food ->
                        val isSelected = selectedFood?.id == food.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFood = food
                                    // При клике подставляем имя в поиск
                                    searchQuery = food.name
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedFood = food
                                    searchQuery = food.name
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(text = food.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = food.categoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedFood?.let { food ->
                        onConfirm(food.name, food.categoryName, food.id)
                    }
                },
                enabled = selectedFood != null
            ) {
                Text(stringResource(R.string.dialog_btn_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_btn_cancel))
            }
        }
    )
}
