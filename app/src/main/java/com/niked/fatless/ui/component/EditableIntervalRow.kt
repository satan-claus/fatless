package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppError
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableIntervalRow(
    index: Int,
    interval: Interval,
    onNameChange: (String) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер интервала (bodySmall: Roboto 13/1.4 Medium)
            Text(
                text = "${index + 1}",
                style = AppTypography.bodySmall,
                color = AppTextTertiary,
                modifier = Modifier.width(28.dp)
            )

            // Поле названия (через BasicTextField для чистоты)
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = interval.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AppTypography.bodyLarge.copy(color = AppTextPrimary),
                    decorationBox = { innerTextField ->
                        if (interval.name.isEmpty()) {
                            Text(
                                text = "Название",
                                color = AppTextTertiary,
                                style = AppTypography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Поле времени (OutlinedTextField для акцента)
            OutlinedTextField(
                value = if (interval.seconds == 0) "" else interval.seconds.toString(),
                onValueChange = { newValue ->
                    val filtered = newValue.filter { char -> char.isDigit() }
                    onSecondsChange(if (filtered.isNotEmpty()) filtered.toInt() else 0)
                },
                modifier = Modifier.width(75.dp),
                textStyle = AppTypography.bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBorder,
                    unfocusedBorderColor = AppBorder,
                    cursorColor = AppTextPrimary
                )
            )

            // Удаление
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AppError,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
