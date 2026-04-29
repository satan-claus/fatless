package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.BuildConfig
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ИСПОЛЬЗУЕМ НАШ УНИВЕРСАЛЬНЫЙ ТОПБАР
        WorkoutTopBar(
            title = "Настройки",
            subTitle = "Конфигурация",
            onBackClick = onBackClick
        )

        Column(modifier = Modifier.padding(24.dp)) {

            // СЕКЦИЯ: ТРЕНИРОВКА
            Text(text = "Тренировка", style = AppTypography.labelMedium, color = AppPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            SettingToggleItem(
                title = "Звуковые сигналы",
                subtitle = "Пищать на последних секундах",
                checked = state.isSoundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )

            SettingToggleItem(
                title = "Автозавершение по цели",
                subtitle = "Переключать интервал при достижении нормы шагов",
                checked = state.autoFinishOnGoal,
                onCheckedChange = { viewModel.toggleAutoFinish(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Цель по шагам",
                style = AppTypography.labelMedium,
                color = AppPrimary
            )

            OutlinedTextField(
                value = state.stepGoal.toString(),
                onValueChange = {
                    // Валидация: только цифры, максимум 6 знаков
                    val filtered = it.filter { char -> char.isDigit() }
                    if (filtered.length <= 6) {
                        viewModel.updateStepGoal(filtered.toIntOrNull() ?: 0)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    Text("шагов", style = AppTypography.bodySmall, color = AppTextTertiary, modifier = Modifier.padding(end = 12.dp))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = AppBorder
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // СЕКЦИЯ: ИНФО
            Text(text = "Приложение", style = AppTypography.labelMedium, color = AppPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Версия ${BuildConfig.VERSION_NAME} (Джон-Эдишн)", style = AppTypography.bodySmall, color = AppTextTertiary)
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = AppTypography.labelLarge, color = AppTextPrimary)
            Text(text = subtitle, style = AppTypography.bodySmall, color = AppTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppPrimary,
                checkedTrackColor = AppPrimary.copy(alpha = 0.5f)
            )
        )
    }
}
