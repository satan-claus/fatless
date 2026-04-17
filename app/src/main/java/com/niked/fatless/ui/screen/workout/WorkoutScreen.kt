package com.niked.fatless.ui.screen.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.domain.model.IntervalType
import com.niked.fatless.ui.viewmodel.WorkoutViewModel
import com.niked.fatless.utils.formatDuration

@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workout = uiState.workout

    // Используем правильное имя поля из WorkoutUiState
    val currentInterval = workout?.intervals?.getOrNull(uiState.currentIntervalIndex)

    Scaffold(
        topBar = {
            // Чтобы можно было выйти из тренировки
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    // Динамический фон: если отдых — оранжевый, если работа — зеленый
                    if (currentInterval?.type == IntervalType.REST) AppOrangeLight else AppPrimaryLight
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentInterval?.name ?: "Загрузка...",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = formatDuration(uiState.timeLeft),
                style = AppTypography.displayLarge // Наш Roboto Mono 68sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.toggleTimer() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRunning) AppOrange else AppPrimary
                )
            ) {
                Text(if (uiState.isRunning) "ПАУЗА" else "СТАРТ")
            }
        }
    }
}