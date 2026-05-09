package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.ui.screen.animateFloatNumberAsState
import com.niked.fatless.ui.screen.animateNumberAsState
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppRed
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorSteps
import com.niked.fatless.ui.viewmodel.NutritionUiState
import kotlinx.coroutines.delay

@Composable
fun DailySummaryCard(
    nutrition: NutritionUiState,
    steps: Int,
    distance: Float,
    burnedCalories: Float,
    stepGoal: Int,
    weight: Float,
    onWeightClick: () -> Unit,
    onClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    // Флаг, чтобы понять: мы только открыли экран или уже работаем
    var isFirstLoad by remember { mutableStateOf(true) }
    var startAnim by remember { mutableStateOf(false) }

    // Этот эффект сработает ТОЛЬКО ОДИН РАЗ при создании карточки
    LaunchedEffect(Unit) {
        // Маленькая пауза для плавности
        delay(100)
        startAnim = true
        // Ждем, пока первичная анимация от 0 до текущих шагов закончится
        delay(1000)
        // Выключаем режим анимации навсегда для этого сеанса
        isFirstLoad = false
    }

    // Если это первая загрузка — анимируем от 0.
    // Если уже нет — берем реальное значение без посредников.
    val displaySteps = if (isFirstLoad) {
        animateNumberAsState(targetValue = if (startAnim) steps else 0).value
    } else {
        steps
    }

    val displayBurned = if (isFirstLoad) {
        animateFloatNumberAsState(targetValue = if (startAnim) burnedCalories else 0f).value
    } else {
        burnedCalories
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NutritionalValueView(
                    proteins = nutrition.totalProteins,
                    fats = nutrition.totalFats,
                    carbs = nutrition.totalCarbs,
                    calories = nutrition.totalCalories.toInt(),
                    size = 100.dp
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_summary_today),
                        style = AppTypography.titleMedium,
                        color = AppTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(
                            R.string.daily_summary_steps_format,
                            displaySteps,
                            stepGoal
                        ),
                        style = AppTypography.labelMedium,
                        color = ColorSteps
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_on_24),
                            contentDescription = stringResource(R.string.content_description_location),
                            modifier = Modifier.size(14.dp),
                            tint = AppRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.daily_summary_distance_format, distance),
                            style = AppTypography.bodySmall,
                            color = AppTextSecondary
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.ic_fire_24),
                            contentDescription = stringResource(R.string.content_description_burned_calories),
                            modifier = Modifier.size(14.dp),
                            tint = AppOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                R.string.daily_summary_burned_format,
                                displayBurned.toInt()
                            ),
                            style = AppTypography.bodySmall,
                            color = AppTextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onWeightClick() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_weight_24dp),
                            contentDescription = stringResource(R.string.content_description_edit_weight),
                            tint = AppSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            // Используем формат из ресурсов
                            text = stringResource(R.string.weight_unit_kg_float, weight),
                            style = AppTypography.bodySmall,
                            color = AppTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OverstepLinearProgress(
                        steps = steps,
                        stepGoal = stepGoal
                    )
                }
            }

            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "История",
                    tint = AppTextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun WeightDialog(
    initialWeight: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var weightText by remember { mutableStateOf(initialWeight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.weight_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { input ->
                        // Заменяем запятую на точку для удобства
                        val cleaned = input.replace(',', '.')
                        // Разрешаем цифры и максимум одну точку
                        if (cleaned.count { it == '.' } <= 1 && cleaned.all { it.isDigit() || it == '.' }) {
                            weightText = cleaned
                        }
                    },
                    label = { Text(stringResource(R.string.setup_weight_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val weight = weightText.toFloatOrNull() ?: initialWeight
                onConfirm(weight)
            }) {
                Text(stringResource(R.string.weight_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.weight_dialog_dismiss))
            }
        }
    )
}

@Composable
fun AddWorkoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.button_create_workout),
            style = AppTypography.labelMedium
        )
    }
}
