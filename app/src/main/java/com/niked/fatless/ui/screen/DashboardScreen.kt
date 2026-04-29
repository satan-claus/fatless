package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.AddWorkoutButton
import com.niked.fatless.ui.component.DailySummaryCard
import com.niked.fatless.ui.component.WorkoutItem
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.component.fatlesshistory.FatLessHistoryComponent
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onWorkoutClick: (String) -> Unit,
    onAddWorkoutClick: () -> Unit,
    onNutritionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsState()
    val nutrition by viewModel.todayNutrition.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val history by viewModel.historyState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
    ) {
        WorkoutTopBar(
            title = "Dashboard",
            subTitle = "Твои показатели сегодня",
            onBackClick = onExitClick,
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, null, tint = AppTextPrimary)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            // 1. КАРТОЧКА-ДАШБОРД (Питание + Шаги)
            item {
                val distance by viewModel.distanceKm.collectAsState()

                DailySummaryCard(
                    nutrition = nutrition,
                    steps = steps,
                    distance = distance,
                    stepGoal = viewModel.stepGoal,
                    onClick = onNutritionClick
                )
            }

            // 2. ИСТОРИЯ (Лента последних дней)
            item {
                FatLessHistoryComponent()
            }

            item {
                Text(
                    text = "Тренировки",
                    style = AppTypography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(workouts) { workout ->
                WorkoutItem(
                    workout = workout,
                    onClick = { onWorkoutClick(workout.id) }
                )
            }

            item {
                AddWorkoutButton(onClick = onAddWorkoutClick)
            }
        }
    }
}
