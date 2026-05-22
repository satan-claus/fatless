package com.niked.fatless.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppSurface,
    background = AppBackground,
    surface = AppSurface,
    error = AppError,
    onSurface = AppTextPrimary,
    onBackground = AppTextPrimary
)

@Composable
fun FatLessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}