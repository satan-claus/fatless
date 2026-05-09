package com.niked.fatless.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.viewmodel.SetupViewModel

@Composable
fun SetupProfileScreen(
    onSuccess: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setup_title),
            style = AppTypography.titleLarge,
            color = AppTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.setup_subtitle),
            style = AppTypography.bodyMedium,
            color = AppTextTertiary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Поле Роста (только цифры)
        OutlinedTextField(
            value = viewModel.height,
            onValueChange = { input ->
                // Разрешаем только цифры
                if (input.all { it.isDigit() }) {
                    viewModel.height = input
                }
            },
            label = { Text(stringResource(R.string.setup_height_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле Веса (цифры и одна точка)
        OutlinedTextField(
            value = viewModel.weight,
            onValueChange = { input ->
                // Заменяем запятую на точку для удобства
                val cleaned = input.replace(',', '.')
                // Разрешаем цифры и максимум одну точку
                if (cleaned.count { it == '.' } <= 1 && cleaned.all { it.isDigit() || it == '.' }) {
                    viewModel.weight = cleaned
                }
            },
            label = { Text(stringResource(R.string.setup_weight_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.saveProfile(onSuccess) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.setup_button))
        }
    }
}