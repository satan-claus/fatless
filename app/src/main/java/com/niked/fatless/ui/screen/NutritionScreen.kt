package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.domain.model.Food
import com.niked.fatless.ui.component.CreateNewFoodHint
import com.niked.fatless.ui.component.DiaryItem
import com.niked.fatless.ui.component.EmptyDiaryHint
import com.niked.fatless.ui.component.FoodInputDialog
import com.niked.fatless.ui.component.FoodResultItem
import com.niked.fatless.ui.component.NutrientInfo
import com.niked.fatless.ui.component.NutritionalValueView
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppError
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorCarbohydrates
import com.niked.fatless.ui.theme.ColorFats
import com.niked.fatless.ui.theme.ColorProteins
import com.niked.fatless.ui.viewmodel.NutritionViewModel
import kotlinx.coroutines.delay

@Composable
fun NutritionScreen(
    onBackClick: () -> Unit,
    onFoodCreateClick: (String) -> Unit,
    onFoodEditClick: (String) -> Unit,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()

    var isSearching by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.totalCalories) {
        startAnim = false
        delay(50)
        startAnim = true
    }

    // Анимируем каждый макрос от 0 до цели
    val animP by animateFloatNumberAsState(
        targetValue = if (startAnim) uiState.totalProteins else 0f
    )
    val animF by animateFloatNumberAsState(
        targetValue = if (startAnim) uiState.totalFats else 0f
    )
    val animC by animateFloatNumberAsState(
        targetValue = if (startAnim) uiState.totalCarbs else 0f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        // 1. ТОПБАР
        WorkoutTopBar(
            title = stringResource(R.string.nutrition_title),
            subTitle = "",
            onBackClick = onBackClick
        )

        // 2. КРУЖОК (Всегда на виду!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            NutritionalValueView(
                proteins = uiState.totalProteins,
                fats = uiState.totalFats,
                carbs = uiState.totalCarbs,
                calories = uiState.totalCalories.toInt(),
                size = 180.dp
            )
        }

        // Инфо-колонка для отображения цифр
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NutrientInfo(label = stringResource(R.string.nutrition_proteins), value = animP, color = ColorProteins)
            NutrientInfo(label = stringResource(R.string.nutrition_fats), value = animF, color = ColorFats)
            NutrientInfo(label = stringResource(R.string.nutrition_carbs), value = animC, color = ColorCarbohydrates)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. ПОЛЕ ПОИСКА
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                viewModel.onQueryChange(it)
                isSearching = it.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text(stringResource(R.string.nutrition_search_placeholder), color = AppTextTertiary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.content_description_search),
                    tint = AppTextSecondary
                )
            },
            trailingIcon = {
                if (isSearching) {
                    IconButton(onClick = {
                        viewModel.onQueryChange("")
                        isSearching = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.content_description_clear_search)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. КОНТЕНТНАЯ ОБЛАСТЬ
        Box(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 24.dp)
        ) {
            if (isSearching) {
                if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                    CreateNewFoodHint(
                        query = searchQuery,
                        onClick = {
                            onFoodCreateClick(searchQuery)
                        }
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults) { food ->
                            FoodResultItem(
                                food = food,
                                onClick = { selectedFood = food },
                                onEditClick = {
                                    onFoodEditClick(food.id)
                                },
                                onDeleteClick = {
                                    viewModel.deleteProductFromLibrary(food.id)
                                }
                            )
                        }
                    }
                }
            } else {
                if (diaryEntries.isEmpty()) {
                    EmptyDiaryHint()
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { Text(stringResource(R.string.nutrition_section_today), style = AppTypography.labelMedium, color = AppPrimary) }
                        items(diaryEntries, key = { it.id }) { entry ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteMeal(entry)
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) AppError else Color.Transparent
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.content_description_delete_meal),
                                            tint = Color.White
                                        )
                                    }
                                }
                            ) {
                                DiaryItem(entry) { viewModel.deleteMeal(entry) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedFood != null) {
        FoodInputDialog(
            food = selectedFood!!,
            onDismiss = { selectedFood = null },
            onConfirm = { amount ->
                viewModel.addMeal(selectedFood!!, amount)
                selectedFood = null
                // Очищаем поиск и закрываем его
                viewModel.onQueryChange("")
                isSearching = false
            }
        )
    }
}
