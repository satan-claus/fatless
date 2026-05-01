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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.domain.model.MeasureUnit
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.FoodFormViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodFormScreen(
    onBackClick: () -> Unit,
    viewModel: FoodFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val allCategories by viewModel.categories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        WorkoutTopBar(
            title = if (state.name.isEmpty()) stringResource(R.string.food_form_title_new) else stringResource(R.string.food_form_title_edit),
            subTitle = if (state.name.isEmpty()) stringResource(R.string.food_form_subtitle_add) else state.name,
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { viewModel.saveProduct(onBackClick) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.content_description_save),
                        tint = AppPrimary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. НАЗВАНИЕ
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.food_form_label_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = AppBorder
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. ЕДИНИЦЫ ИЗМЕРЕНИЯ
            Text(stringResource(R.string.food_form_section_unit), style = AppTypography.labelMedium, color = AppTextSecondary)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MeasureUnit.entries.forEach { unit ->
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

            // 3. БЖУ
            Text(stringResource(R.string.food_form_section_macros), style = AppTypography.labelMedium, color = AppTextSecondary)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionField(stringResource(R.string.food_form_macro_p_short), state.proteins, ColorProteins, Modifier.weight(1f), viewModel::updateProteins)
                NutritionField(stringResource(R.string.food_form_macro_f_short), state.fats, ColorFats, Modifier.weight(1f), viewModel::updateFats)
                NutritionField(stringResource(R.string.food_form_macro_c_short), state.carbs, ColorCarbohydrates, Modifier.weight(1f), viewModel::updateCarbs)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. КАЛОРИИ
            OutlinedTextField(
                value = state.calories,
                onValueChange = viewModel::updateCalories,
                label = { Text(stringResource(R.string.food_form_label_calories)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = AppBorder
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. КАТЕГОРИИ
            Text(stringResource(R.string.food_form_section_category), style = AppTypography.labelMedium, color = AppTextSecondary)
            FlowRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCategories.forEach { cat ->
                    FilterChip(
                        selected = state.categoryId == cat.id,
                        onClick = { viewModel.updateCategory(cat.id) },
                        label = { Text(cat.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppPrimary.copy(alpha = 0.1f),
                            selectedLabelColor = AppPrimary
                        )
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
