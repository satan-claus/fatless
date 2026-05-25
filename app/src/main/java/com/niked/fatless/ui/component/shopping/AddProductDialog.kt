package com.niked.fatless.ui.component.shopping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.niked.fatless.R
import com.niked.fatless.domain.model.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    // Передаем список доступных продуктов из базы
    foodItems: List<FoodItem>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, foodId: String) -> Unit
) {
    // Стейты для управления выпадающим меню
    var expanded by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_product_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Выпадающий контейнер Material 3
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        // Показываем имя выбранного продукта или подсказку
                        value = selectedFood?.name ?: stringResource(R.string.dialog_select_food_placeholder),
                        onValueChange = {},
                        readOnly = true, // Запрещаем ввод букв руками
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        foodItems.forEach { food ->
                            DropdownMenuItem(
                                text = { Text("${food.name} (${food.categoryName})") },
                                onClick = {
                                    selectedFood = food
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedFood?.let { food ->
                        // Продукт выбран — пробрасываем его реальные параметры и строковый ID (например, "m1")
                        onConfirm(food.name, food.categoryName, food.id)
                    }
                },
                // Кнопка заблокирована, пока юзер не выберет продукт
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
