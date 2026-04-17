package com.niked.fatless.ui.screen.workoutlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.viewmodel.WorkoutListUiState
import com.niked.fatless.ui.viewmodel.WorkoutListViewModel

@Composable
fun WorkoutListScreen(
    viewModel: WorkoutListViewModel,
    onWorkoutClick: (String) -> Unit,
    onAddWorkoutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            // Наш кастомный TopBar (помнишь, мы его обсуждали?)
            Text("FatLess", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineLarge)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWorkoutClick) {
                Text("+")
            }
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is WorkoutListUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is WorkoutListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.workouts) { workout ->
                            // Наша карточка WorkoutRow
                            Button(onClick = { onWorkoutClick(workout.id) }) {
                                Text(workout.title)
                            }
                        }
                    }
                }
                is WorkoutListUiState.Error -> Text(s.message)
            }
        }
    }
}