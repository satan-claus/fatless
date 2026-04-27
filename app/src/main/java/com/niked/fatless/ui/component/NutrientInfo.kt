package com.niked.fatless.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun NutrientInfo(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${value.toInt()}г",
            style = AppTypography.labelLarge,
            color = color
        )
        Text(
            text = label,
            style = AppTypography.bodySmall,
            color = AppTextTertiary
        )
    }
}