package com.niked.fatless.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.components.EditableIntervalRow
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.WorkoutCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCreateScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutCreateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Новая тренировка", style = AppTypography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save { onBackClick() } },
                        enabled = state.title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AppPrimary)
                    }
                },
                // ИСПРАВЛЕНО: Теперь цвета задаются через TopAppBarDefaults
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBackground
                )
            )
        },
        containerColor = AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Название тренировки") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            itemsIndexed(state.intervals) { index, interval ->
                EditableIntervalRow(
                    interval = interval,
                    onNameChange = { newName ->
                        viewModel.updateInterval(index, interval.copy(name = newName))
                    },
                    onSecondsChange = { newSecs ->
                        viewModel.updateInterval(index, interval.copy(seconds = newSecs))
                    },
                    onDelete = { viewModel.deleteInterval(index) }
                )
            }

            item {
                Button(
                    onClick = { viewModel.addInterval() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppSecondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить интервал")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}