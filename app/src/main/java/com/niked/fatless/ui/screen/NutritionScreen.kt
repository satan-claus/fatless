package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.NutritionalValueView
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.NutritionViewModel

@Composable
fun NutritionScreen(
    onBackClick: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Питание за сегодня",
            style = AppTypography.titleMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // 1. НАШ КРУТОЙ КРУЖОК
        NutritionalValueView(
            proteins = state.proteins,
            fats = state.fats,
            carbs = state.carbs,
            calories = viewModel.calculateCalories(),
            size = 220.dp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 2. СЛАЙДЕРЫ ДЛЯ ТЕСТА
        NutritionTestSlider("Белки (г)", state.proteins, ColorProteins) { viewModel.updateProteins(it) }
        NutritionTestSlider("Жиры (г)", state.fats, ColorFats) { viewModel.updateFats(it) }
        NutritionTestSlider("Углеводы (г)", state.carbs, ColorCarbohydrates) { viewModel.updateCarbs(it) }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Двигай ползунки, чтобы проверить анимацию",
            style = AppTypography.bodySmall,
            color = AppTextTertiary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun NutritionTestSlider(label: String, value: Float, color: androidx.compose.ui.graphics.Color, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = AppTypography.bodySmall, color = AppTextSecondary)
            Text(text = value.toInt().toString(), style = AppTypography.bodySmall, color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..200f,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}
