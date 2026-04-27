package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.domain.model.Food
import com.niked.fatless.ui.component.AddFoodDialog
import com.niked.fatless.ui.component.DiaryItem
import com.niked.fatless.ui.component.EmptyDiaryHint
import com.niked.fatless.ui.component.FoodResultItem
import com.niked.fatless.ui.component.NutritionalValueView
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.NutritionViewModel

@Composable
fun NutritionScreen(
    onBackClick: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()

    var isSearching by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        // 1. ТОПБАР
        WorkoutTopBar(
            title = "Дневник питания",
            subTitle = "",
            onBackClick = onBackClick
        )

        // 2. КРУЖОК (Всегда на виду!)
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            NutritionalValueView(
                proteins = uiState.totalProteins,
                fats = uiState.totalFats,
                carbs = uiState.totalCarbs,
                calories = uiState.totalCalories,
                size = 180.dp
            )
        }

        // 3. ПОЛЕ ПОИСКА (Джон начинает писать — список внизу меняется)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                viewModel.onQueryChange(it)
                isSearching = it.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text("Что съели?", color = AppTextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AppTextSecondary) },
            trailingIcon = {
                if (isSearching) {
                    IconButton(onClick = {
                        viewModel.onQueryChange("")
                        isSearching = false
                    }) { Icon(Icons.Default.Close, null) }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. КОНТЕНТНАЯ ОБЛАСТЬ (Либо поиск, либо дневник)
        Box(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
            if (isSearching) {
                // Список результатов поиска
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { food ->
                        FoodResultItem(food) { selectedFood = food }
                    }
                }
            } else {
                // Список съеденного за сегодня
                if (diaryEntries.isEmpty()) {
                    EmptyDiaryHint()
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { Text("СЕГОДНЯ:", style = AppTypography.labelMedium, color = AppPrimary) }
                        items(diaryEntries) { entry ->
                            DiaryItem(entry) { viewModel.deleteMeal(entry.id) }
                        }
                    }
                }
            }
        }
    }

    // Диалог ввода веса (поверх всего)
    if (selectedFood != null) {
        AddFoodDialog(
            foodName = selectedFood!!.name,
            onDismiss = { selectedFood = null },
            onConfirm = { weight ->
                viewModel.addMeal(selectedFood!!, weight)
                selectedFood = null
                viewModel.onQueryChange("") // Очищаем поиск
                isSearching = false
            }
        )
    }
}

//@Composable
//fun NutritionTestSlider(label: String, value: Float, color: androidx.compose.ui.graphics.Color, onValueChange: (Float) -> Unit) {
//    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            Text(text = label, style = AppTypography.bodySmall, color = AppTextSecondary)
//            Text(text = value.toInt().toString(), style = AppTypography.bodySmall, color = color)
//        }
//        Slider(
//            value = value,
//            onValueChange = onValueChange,
//            valueRange = 0f..200f,
//            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
//        )
//    }
//}
