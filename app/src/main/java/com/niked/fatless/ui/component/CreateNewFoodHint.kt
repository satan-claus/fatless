package com.niked.fatless.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun CreateNewFoodHint(
    query: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Продукт '$query' не найден",
            style = AppTypography.titleSmall,
            color = AppTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Нажмите здесь, чтобы добавить его\nв свой справочник",
            style = AppTypography.bodySmall,
            color = AppTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
