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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.BuildConfig
import com.niked.fatless.R
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppDisabledBg
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppSurface
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

    var showFirstLaunchDialog by remember { mutableStateOf(viewModel.isFirstLaunch()) }

    if (showFirstLaunchDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setFirstLaunchDone()
                    showFirstLaunchDialog = false
                }) {
                    Text(stringResource(R.string.settings_dialog_confirm), color = AppPrimary, style = AppTypography.labelMedium)
                }
            },
            title = { Text(stringResource(R.string.settings_dialog_title), style = AppTypography.titleMedium) },
            text = { Text(stringResource(R.string.settings_dialog_text)) },
            containerColor = AppSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        WorkoutTopBar(
            title = stringResource(R.string.settings_title),
            subTitle = stringResource(R.string.settings_subtitle),
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- СЕКЦИЯ: БИОМЕТРИЯ ---
            Text(text = stringResource(R.string.settings_section_biometrics), style = AppTypography.labelMedium, color = AppPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // РОСТ
                OutlinedTextField(
                    value = if (state.userHeight == 0) "" else state.userHeight.toString(),
                    onValueChange = { val v = it.filter { c -> c.isDigit() }; if (v.length <= 3) viewModel.updateHeight(v.toIntOrNull() ?: 0) },
                    label = { Text(stringResource(R.string.settings_label_height)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Text(stringResource(R.string.settings_unit_cm), style = AppTypography.bodySmall) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPrimary, unfocusedBorderColor = AppBorder)
                )
                // ВЕС
                OutlinedTextField(
                    value = if (state.userWeight == 0) "" else state.userWeight.toString(),
                    onValueChange = { val v = it.filter { c -> c.isDigit() }; if (v.length <= 3) viewModel.updateWeight(v.toIntOrNull() ?: 0) },
                    label = { Text(stringResource(R.string.settings_label_weight)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Text(stringResource(R.string.settings_unit_kg), style = AppTypography.bodySmall) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPrimary, unfocusedBorderColor = AppBorder)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- СЕКЦИЯ: ТРЕНИРОВКА ---
            Text(text = stringResource(R.string.settings_section_workout), style = AppTypography.labelMedium, color = AppPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            SettingToggleItem(
                title = stringResource(R.string.settings_sound_title),
                subtitle = stringResource(R.string.settings_sound_subtitle),
                checked = state.isSoundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )

            if (state.isSoundEnabled) {
                SoundVolumeSettings(volume = state.soundVolume, onVolumeChange = { viewModel.updateVolume(it) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingToggleItem(
                title = stringResource(R.string.settings_auto_finish_title),
                subtitle = stringResource(R.string.settings_auto_finish_subtitle),
                checked = state.autoFinishOnGoal,
                onCheckedChange = { viewModel.toggleAutoFinish(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- СЕКЦИЯ: ЦЕЛЬ ---
            Text(text = stringResource(R.string.settings_section_goal), style = AppTypography.labelMedium, color = AppPrimary)
            OutlinedTextField(
                value = state.stepGoal.toString(),
                onValueChange = {
                    val filtered = it.filter { char -> char.isDigit() }
                    if (filtered.length <= 6) viewModel.updateStepGoal(filtered.toIntOrNull() ?: 0)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = { Text(stringResource(R.string.settings_unit_steps), style = AppTypography.bodySmall, modifier = Modifier.padding(end = 12.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPrimary, unfocusedBorderColor = AppBorder)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- СЕКЦИЯ: ИНФО ---
            HorizontalDivider(thickness = 1.dp, color = AppBorder)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.settings_section_app), style = AppTypography.labelMedium, color = AppPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_version_format, BuildConfig.VERSION_NAME),
                style = AppTypography.bodySmall,
                color = AppTextTertiary
            )
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

@Composable
fun SoundVolumeSettings(
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    val percentage = (volume * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_volume_label),
                style = AppTypography.bodySmall,
                color = AppTextSecondary
            )
            Text(
                text = stringResource(R.string.settings_volume_format, percentage),
                style = AppTypography.labelSmall,
                color = AppPrimary
            )
        }
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = AppPrimary,
                activeTrackColor = AppPrimary,
                inactiveTrackColor = AppDisabledBg
            )
        )
    }
}



