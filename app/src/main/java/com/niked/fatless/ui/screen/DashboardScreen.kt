package com.niked.fatless.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.ui.component.AddWorkoutButton
import com.niked.fatless.ui.component.DailySummaryCard
import com.niked.fatless.ui.component.WeightDialog
import com.niked.fatless.ui.component.WorkoutItem
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.component.fatlesshistory.FatLessHistoryComponent
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorTrackRun
import com.niked.fatless.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onAddWorkoutClick: () -> Unit,
    onEditWorkoutClick: (String) -> Unit,
    onExitClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNutritionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShoppingClick: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val workouts by viewModel.workouts.collectAsState()
    val nutrition by viewModel.todayNutrition.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val burnedCaloriesState by viewModel.burnedCalories.collectAsState()
    val currentWeight by viewModel.weight.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }
    val isTracking by viewModel.isTracking.collectAsState()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleTracking(context) },
                containerColor = if (isTracking) ColorTrackRun else AppSecondary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(if(isTracking) R.drawable.ic_stop_circle_24dp else R.drawable.ic_play_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .navigationBarsPadding()
        ) {
            WorkoutTopBar(
                title = stringResource(R.string.dashboard_title),
                subTitle = stringResource(R.string.dashboard_subtitle),
                onBackClick = onExitClick,
                actions = {
                    // 🛒 КНОПКА СПИСКА ПОКУПОК
                    IconButton(onClick = onShoppingClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(R.string.content_description_open_shopping_list),
                            tint = AppPrimary
                        )
                    }

                    // ⚙️ КНОПКА НАСТРОЕК
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.content_description_settings),
                            tint = AppTextTertiary
                        )
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
                item {
                    val distance by viewModel.distanceKm.collectAsState()

                    DailySummaryCard(
                        nutrition = nutrition,
                        steps = steps,
                        distance = distance,
                        burnedCalories = burnedCaloriesState,
                        stepGoal = viewModel.stepGoal,
                        weight = currentWeight,
                        onWeightClick = { showWeightDialog = true },
                        onClick = onNutritionClick,
                        onHistoryClick = onHistoryClick
                    )

                    if (showWeightDialog) {
                        // Вызываем простенький диалог с TextField
                        WeightDialog(
                            initialWeight = currentWeight.toFloat(),
                            onDismiss = { showWeightDialog = false },
                            onConfirm = {
                                viewModel.updateWeight(it)
                                showWeightDialog = false
                            }
                        )
                    }
                }

                item {
                    FatLessHistoryComponent()
                }

                item {
                    Text(
                        text = stringResource(R.string.dashboard_workouts_title),
                        style = AppTypography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(workouts) { workout ->
                    WorkoutItem(
                        workout = workout,
                        onClick = { onWorkoutClick(workout.id) },
                        onEditClick = { onEditWorkoutClick(workout.id) }
                    )
                }

                item {
                    AddWorkoutButton(onClick = onAddWorkoutClick)
                }
            }
        }
    }
}
