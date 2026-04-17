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
import com.niked.fatless.domain.model.WorkoutState
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppOrangeLight
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppPrimaryLight
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.viewmodel.WorkoutViewModel
import com.niked.fatless.utils.formatDuration

@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workout = uiState.workout
    val currentInterval = workout?.intervals?.getOrNull(uiState.currentIntervalIndex)

    // Определяем цвета и тексты в зависимости от состояния
    val isRunning = uiState.status is WorkoutState.RUNNING
    val isFinished = uiState.status is WorkoutState.COMPLETED
    val isRest = currentInterval?.type == IntervalType.REST

    Scaffold(
        topBar = {
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
                    when (uiState.status) {
                        is WorkoutState.COMPLETED -> AppSecondary
                        is WorkoutState.RUNNING, is WorkoutState.PAUSED -> {
                            if (isRest) AppOrangeLight else AppPrimaryLight
                        }
                        else -> AppBackground
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isFinished) {
                Text("ТРЕНИРОВКА ОКОНЧЕНА", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBackClick) { Text("В МЕНЮ") }
            } else {
                Text(
                    text = currentInterval?.name ?: "Загрузка...",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = formatDuration(uiState.timeLeft),
                    style = AppTypography.displayLarge
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.toggleTimer() },
                    colors = ButtonDefaults.buttonColors(
                        // Если тикает — кнопка оранжевая (пауза), если стоит — зеленая (старт)
                        containerColor = if (isRunning) AppOrange else AppPrimary
                    )
                ) {
                    Text(if (isRunning) "ПАУЗА" else "СТАРТ")
                }
            }
        }
    }
}