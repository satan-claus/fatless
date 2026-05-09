package com.niked.fatless.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.ui.theme.AppRed
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTypography

@Composable
fun DaysOfWeekHeader() {
    val days = listOf(
        R.string.day_mon_short to R.string.content_description_day_mon,
        R.string.day_tue_short to R.string.content_description_day_tue,
        R.string.day_wed_short to R.string.content_description_day_wed,
        R.string.day_thu_short to R.string.content_description_day_thu,
        R.string.day_fri_short to R.string.content_description_day_fri,
        R.string.day_sat_short to R.string.content_description_day_sat,
        R.string.day_sun_short to R.string.content_description_day_sun
    )

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        days.forEach { (shortRes, fullRes) ->
            val fullDayName = stringResource(fullRes)
            Text(
                text = stringResource(shortRes),
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = fullDayName },
                textAlign = TextAlign.Center,
                style = AppTypography.labelMedium,
                color = if (shortRes == R.string.day_sat_short || shortRes == R.string.day_sun_short) AppRed else AppTextSecondary
            )
        }
    }
}

@Composable
fun DayItem(
    day: Int,
    isSelected: Boolean,
    hasData: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AppSecondary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = AppTypography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    isToday -> AppSecondary
                    else -> AppTextPrimary
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasData) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else AppSecondary)
                )
            }
        }
    }
}
