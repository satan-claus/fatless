package com.niked.fatless.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.ui.component.EditableIntervalRow
import com.niked.fatless.ui.theme.*
import com.niked.fatless.ui.viewmodel.WorkoutCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCreateScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutCreateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Новая тренировка", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveWorkout { onBackClick() } },
                        enabled = state.title.isNotBlank() && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = if (state.title.isNotBlank()) AppPrimary else AppTextTertiary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Название",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTextTertiary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Например: Утренняя йога") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = AppSurface,
                        focusedContainerColor = AppSurface,
                        unfocusedBorderColor = AppBorder,
                        focusedBorderColor = AppPrimary,
                        cursorColor = AppPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Интервалы",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTextTertiary
                )
            }

            itemsIndexed(state.intervals) { index, interval ->
                EditableIntervalRow(
                    index = index,
                    interval = interval,
                    onNameChange = { name -> viewModel.updateInterval(index, name, interval.seconds) },
                    onSecondsChange = { secs -> viewModel.updateInterval(index, interval.name, secs) },
                    onRemove = { viewModel.removeInterval(index) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addInterval() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить интервал")
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
