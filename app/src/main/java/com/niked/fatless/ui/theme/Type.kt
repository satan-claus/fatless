package com.niked.fatless.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    // Timer Display: Roboto Mono 68/1.0 Bold
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 68.sp,
        lineHeight = 68.sp
    ),
    // H1: Roboto 26/1.25 Bold
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.5.sp // 26 * 1.25
    ),
    // Title: Roboto 16/1.3 SemiBold
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.8.sp // 16 * 1.3
    ),
    // Body: Roboto 15/1.5 Regular
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.5.sp
    ),
    // Label: Roboto 14/1.4 Medium (Названия интервалов)
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.6.sp
    ),
    // Button: Roboto 15/1.0 SemiBold
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 15.sp
    ),
    // Caption: Roboto 13/1.4 Medium
    bodySmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.2.sp
    ),
    // State: Roboto 11/1.0 Bold CAPS ls1.5
    // Используем для мелких меток состояний
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 11.sp,
        letterSpacing = 1.5.sp
    )
)