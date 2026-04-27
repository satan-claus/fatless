package com.niked.fatless.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun AddFoodDialog(foodName: String, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var weight by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = foodName, style = AppTypography.titleMedium) },
        text = {
            Column {
                Text("Сколько грамм съели?", style = AppTypography.bodySmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.all { char -> char.isDigit() }) weight = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(weight.toIntOrNull() ?: 100) }) {
                Text("ДОБАВИТЬ", color = AppPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ОТМЕНА", color = AppTextTertiary) }
        }
    )
}