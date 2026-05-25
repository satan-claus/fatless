package com.niked.fatless.ui.component.shopping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.niked.fatless.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddShopDialog(
    // Передаем список всех уникальных категорий из приложения
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, categories: List<String>, radius: Float, lat: Double, lon: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var radiusStr by remember { mutableStateOf("") }
    var latStr by remember { mutableStateOf("") }
    var lonStr by remember { mutableStateOf("") }

    // Реактивный список для хранения выбранных категорий (наших скиллов)
    val selectedCategories = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_shop_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Защита от наплыва клавиатуры
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_field_shop_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Секция выбора категорий
                Text(
                    text = stringResource(R.string.dialog_select_categories_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // FlowRow автоматически переносит чипсы на новую строку, если они не влезают по ширине
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategories.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedCategories.remove(category)
                                } else {
                                    selectedCategories.add(category)
                                }
                            },
                            label = { Text(category) },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = radiusStr,
                    onValueChange = { radiusStr = it },
                    label = { Text(stringResource(R.string.dialog_field_radius)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = latStr,
                    onValueChange = { latStr = it },
                    label = { Text(stringResource(R.string.dialog_field_latitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lonStr,
                    onValueChange = { lonStr = it },
                    label = { Text(stringResource(R.string.dialog_field_longitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val radius = radiusStr.toFloatOrNull() ?: 300f
                    val lat = latStr.toDoubleOrNull() ?: 0.0
                    val lon = lonStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && selectedCategories.isNotEmpty()) {
                        // Возвращаем чистый список выбранных категорий наружу
                        onConfirm(name, selectedCategories.toList(), radius, lat, lon)
                    }
                },
                // Кнопка заблокирована, пока не ввели имя и не выбрали ХОТЯ БЫ ОДИН чипс-скилл
                enabled = name.isNotBlank() && selectedCategories.isNotEmpty()
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
