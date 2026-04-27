package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.domain.model.MeasureUnit
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorCarbohydrates
import com.niked.fatless.ui.theme.ColorFats
import com.niked.fatless.ui.theme.ColorProteins
import com.niked.fatless.ui.viewmodel.FoodCreateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodCreateScreen(
    onBackClick: () -> Unit,
    viewModel: FoodCreateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        WorkoutTopBar(
            title = "Новый продукт",
            subTitle = "Добавить в справочник",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { viewModel.saveProduct(onBackClick) }) {
                    Icon(Icons.Default.Check, null, tint = AppPrimary)
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. ИМЯ
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("Название продукта") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. ЕДИНИЦЫ ИЗМЕРЕНИЯ
            Text("Единица измерения:", style = AppTypography.labelMedium, color = AppTextSecondary)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MeasureUnit.values().forEach { unit ->
                    FilterChip(
                        selected = state.unit == unit,
                        onClick = { viewModel.updateUnit(unit) },
                        label = { Text(unit.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = AppPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. БЖУ (В ряд)
            Text("БЖУ на 100г (или 1шт):", style = AppTypography.labelMedium, color = AppTextSecondary)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionField("Б", state.proteins, ColorProteins, Modifier.weight(1f), viewModel::updateProteins)
                NutritionField("Ж", state.fats, ColorFats, Modifier.weight(1f), viewModel::updateFats)
                NutritionField("У", state.carbs, ColorCarbohydrates, Modifier.weight(1f), viewModel::updateCarbs)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. КАЛОРИИ
            OutlinedTextField(
                value = state.calories,
                onValueChange = viewModel::updateCalories,
                label = { Text("Калории (кКал)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. КАТЕГОРИИ (FlowRow бы тут, но сделаем через Row со скроллом)
            Text("Категория:", style = AppTypography.labelMedium, color = AppTextSecondary)
            FlowRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.categories.forEach { cat ->
                    FilterChip(
                        selected = state.category == cat,
                        onClick = { viewModel.updateCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun NutritionField(label: String, value: String, color: Color, modifier: Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            unfocusedBorderColor = AppBorder
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
