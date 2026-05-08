package com.niked.fatless.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.niked.fatless.domain.model.ExerciseType
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun ExerciseModeSelector(
    modifier: Modifier = Modifier,
    types: List<ExerciseType>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { type ->
            val isSelected = type.id == selectedId

            FilterChip(
                selected = isSelected,
                onClick = { onSelect(type.id) },
                label = {
                    Text(
                        text = stringResource(type.nameResId),
                        style = AppTypography.labelSmall
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(type.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) AppPrimary else AppTextSecondary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppPrimary.copy(alpha = 0.1f),
                    selectedLabelColor = AppPrimary,
                    selectedLeadingIconColor = AppPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = AppPrimary,
                    borderColor = AppBorder,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.5.dp
                )
            )
        }
    }
}