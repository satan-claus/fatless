package com.niked.fatless.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun EmptyDiaryHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppBorder
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.nutrition_diary_empty_title),
            style = AppTypography.titleSmall,
            color = AppTextSecondary
        )
        Text(
            text = stringResource(R.string.nutrition_diary_empty_subtitle),
            style = AppTypography.bodySmall,
            color = AppTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}